"""
FastAPI para o serviço de geração de podcasts.
Plataforma Darcy - Feature Podcastfy

Para executar:
    uvicorn src.podcast_api:app --host 0.0.0.0 --port 8001 --reload
"""

from fastapi import FastAPI, UploadFile, File, HTTPException, BackgroundTasks
from fastapi.responses import FileResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import tempfile
import os
import sys
import uuid
from datetime import datetime
from typing import Dict, Optional
import asyncio

# Adiciona o diretório src ao path para imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from podcast_service import get_podcast_service, PodcastService

# Configuração da aplicação
app = FastAPI(
    title="Podcastfy API - Plataforma Darcy",
    description="API para geração de podcasts educativos a partir de PDFs",
    version="1.0.0"
)

# CORS para permitir chamadas do backend Java
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Diretório para armazenar podcasts gerados temporariamente
PODCAST_OUTPUT_DIR = tempfile.mkdtemp(prefix="podcastfy_")

# Armazena status dos jobs em andamento
podcast_jobs: Dict[str, dict] = {}


@app.get("/")
def root():
    """Endpoint raiz com informações da API."""
    return {
        "service": "Podcastfy API",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health")
def health_check():
    """Health check para monitoramento."""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat()
    }


@app.post("/generate-podcast")
async def generate_podcast_sync(file: UploadFile = File(...)):
    """
    Gera um podcast a partir de um arquivo PDF (síncrono).
    
    Este endpoint processa o PDF e retorna o áudio diretamente.
    Para PDFs grandes, use o endpoint assíncrono /generate-podcast-async.
    
    Args:
        file: Arquivo PDF para processar
        
    Returns:
        Arquivo de áudio MP3
    """
    # Validação do arquivo
    if not file.filename.lower().endswith('.pdf'):
        raise HTTPException(
            status_code=400, 
            detail="Apenas arquivos PDF são aceitos"
        )
    
    # Salvar PDF temporariamente
    pdf_path = None
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as tmp:
            content = await file.read()
            
            # Verificar tamanho (máximo 50MB)
            if len(content) > 50 * 1024 * 1024:
                raise HTTPException(
                    status_code=400,
                    detail="Arquivo muito grande. Máximo permitido: 50MB"
                )
            
            tmp.write(content)
            pdf_path = tmp.name
        
        # Gerar podcast
        service = get_podcast_service()
        audio_path = service.generate_from_pdf(pdf_path)
        
        # Retornar arquivo de áudio
        return FileResponse(
            audio_path,
            media_type="audio/mpeg",
            filename=f"podcast_{file.filename.replace('.pdf', '')}.mp3"
        )
        
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Erro interno: {str(e)}"
        )
    finally:
        # Limpar arquivo temporário
        if pdf_path and os.path.exists(pdf_path):
            os.unlink(pdf_path)


@app.post("/generate-podcast-async")
async def generate_podcast_async(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...)
):
    """
    Inicia geração de podcast de forma assíncrona.
    
    Retorna um job_id que pode ser usado para verificar o status
    e baixar o resultado quando pronto.
    
    Args:
        file: Arquivo PDF para processar
        
    Returns:
        JSON com job_id para acompanhamento
    """
    if not file.filename.lower().endswith('.pdf'):
        raise HTTPException(
            status_code=400,
            detail="Apenas arquivos PDF são aceitos"
        )
    
    # Gerar ID único para o job
    job_id = str(uuid.uuid4())
    
    # Salvar PDF
    pdf_path = os.path.join(PODCAST_OUTPUT_DIR, f"{job_id}.pdf")
    content = await file.read()
    
    with open(pdf_path, "wb") as f:
        f.write(content)
    
    # Registrar job
    podcast_jobs[job_id] = {
        "status": "processing",
        "filename": file.filename,
        "created_at": datetime.now().isoformat(),
        "audio_path": None,
        "error": None
    }
    
    # Iniciar processamento em background
    background_tasks.add_task(process_podcast_job, job_id, pdf_path)
    
    return JSONResponse({
        "job_id": job_id,
        "status": "processing",
        "message": "Processamento iniciado. Use /job/{job_id} para verificar status."
    })


async def process_podcast_job(job_id: str, pdf_path: str):
    """Processa um job de geração de podcast em background."""
    try:
        service = get_podcast_service()
        audio_path = service.generate_from_pdf(pdf_path)
        
        podcast_jobs[job_id]["status"] = "completed"
        podcast_jobs[job_id]["audio_path"] = audio_path
        
    except Exception as e:
        podcast_jobs[job_id]["status"] = "failed"
        podcast_jobs[job_id]["error"] = str(e)
    finally:
        # Limpar PDF
        if os.path.exists(pdf_path):
            os.unlink(pdf_path)


@app.get("/job/{job_id}")
async def get_job_status(job_id: str):
    """
    Verifica o status de um job de geração de podcast.
    
    Args:
        job_id: ID do job retornado pelo endpoint async
        
    Returns:
        Status do job (processing, completed, failed)
    """
    if job_id not in podcast_jobs:
        raise HTTPException(status_code=404, detail="Job não encontrado")
    
    job = podcast_jobs[job_id]
    return {
        "job_id": job_id,
        "status": job["status"],
        "filename": job["filename"],
        "created_at": job["created_at"],
        "error": job["error"]
    }


@app.get("/job/{job_id}/download")
async def download_podcast(job_id: str):
    """
    Baixa o podcast gerado.
    
    Args:
        job_id: ID do job
        
    Returns:
        Arquivo de áudio MP3
    """
    if job_id not in podcast_jobs:
        raise HTTPException(status_code=404, detail="Job não encontrado")
    
    job = podcast_jobs[job_id]
    
    if job["status"] == "processing":
        raise HTTPException(
            status_code=202,
            detail="Podcast ainda está sendo gerado"
        )
    
    if job["status"] == "failed":
        raise HTTPException(
            status_code=500,
            detail=f"Falha na geração: {job['error']}"
        )
    
    if not job["audio_path"] or not os.path.exists(job["audio_path"]):
        raise HTTPException(
            status_code=404,
            detail="Arquivo de áudio não encontrado"
        )
    
    return FileResponse(
        job["audio_path"],
        media_type="audio/mpeg",
        filename=f"podcast_{job['filename'].replace('.pdf', '')}.mp3"
    )


# Limpeza periódica de jobs antigos (executar a cada hora em produção)
@app.on_event("startup")
async def startup_event():
    """Evento de inicialização da aplicação."""
    print("Podcastfy API iniciada!")
    print(f"Diretorio de output: {PODCAST_OUTPUT_DIR}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
