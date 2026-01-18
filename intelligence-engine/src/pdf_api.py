"""
PDF Management API - Central de Gerenciamento de PDFs
Endpoints para listar, upload, deletar, preview e editar páginas de PDFs.
"""

import os
import io
import base64
import shutil
from pathlib import Path
from typing import List, Optional

import fitz  # PyMuPDF
from fastapi import FastAPI, UploadFile, File, HTTPException, Query
from fastapi.responses import JSONResponse, StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(title="PDF Management API", version="1.0.0")

# CORS para permitir chamadas do backend Java
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Diretório de PDFs (relativo ao intelligence-engine)
PDF_DIR = Path(__file__).parent.parent / "input"
PREVIEW_CACHE = Path(__file__).parent.parent / "preview_cache"

# Garantir que os diretórios existam
PDF_DIR.mkdir(exist_ok=True)
PREVIEW_CACHE.mkdir(exist_ok=True)


# ============== MODELS ==============

class PdfInfo(BaseModel):
    filename: str
    size_bytes: int
    page_count: int
    
class PageInfo(BaseModel):
    page_number: int
    width: int
    height: int

class PdfDetail(BaseModel):
    filename: str
    size_bytes: int
    page_count: int
    pages: List[PageInfo]

class EditPageRequest(BaseModel):
    action: str  # "delete", "insert_blank", "insert_from_pdf"
    page_number: int
    source_pdf: Optional[str] = None
    source_page: Optional[int] = None


class QuestaoReconstruct(BaseModel):
    numero: int
    texto: str
    imagem_base64: Optional[str] = None  # Imagem em base64 (PNG/JPG)


class ReconstructPageRequest(BaseModel):
    page_number: int  # Página a ser substituída
    texto_base: Optional[str] = None  # Texto de apoio/contexto do bloco
    questoes: List[QuestaoReconstruct]


# ============== ENDPOINTS ==============

@app.get("/api/pdfs", response_model=List[PdfInfo])
def list_pdfs():
    """Lista todos os PDFs na pasta input."""
    pdfs = []
    for file in PDF_DIR.glob("*.pdf"):
        try:
            doc = fitz.open(file)
            pdfs.append(PdfInfo(
                filename=file.name,
                size_bytes=file.stat().st_size,
                page_count=len(doc)
            ))
            doc.close()
        except Exception as e:
            # PDF corrompido ou inacessível
            pdfs.append(PdfInfo(
                filename=file.name,
                size_bytes=file.stat().st_size,
                page_count=-1  # Indica erro
            ))
    return sorted(pdfs, key=lambda x: x.filename)


@app.get("/api/pdfs/{filename}", response_model=PdfDetail)
def get_pdf_detail(filename: str):
    """Retorna detalhes de um PDF específico."""
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        doc = fitz.open(file_path)
        pages = []
        for i, page in enumerate(doc):
            rect = page.rect
            pages.append(PageInfo(
                page_number=i + 1,
                width=int(rect.width),
                height=int(rect.height)
            ))
        
        result = PdfDetail(
            filename=filename,
            size_bytes=file_path.stat().st_size,
            page_count=len(doc),
            pages=pages
        )
        doc.close()
        return result
    except Exception as e:
        raise HTTPException(500, f"Erro ao ler PDF: {str(e)}")


@app.post("/api/pdfs/upload")
async def upload_pdf(file: UploadFile = File(...)):
    """Faz upload de um novo PDF."""
    if not file.filename.lower().endswith(".pdf"):
        raise HTTPException(400, "Apenas arquivos PDF são permitidos")
    
    # Sanitiza o nome do arquivo
    safe_name = "".join(c for c in file.filename if c.isalnum() or c in "._- ").strip()
    if not safe_name:
        safe_name = "uploaded.pdf"
    
    dest_path = PDF_DIR / safe_name
    
    # Se já existe, adiciona sufixo
    counter = 1
    original_name = dest_path.stem
    while dest_path.exists():
        dest_path = PDF_DIR / f"{original_name}_{counter}.pdf"
        counter += 1
    
    try:
        with open(dest_path, "wb") as f:
            content = await file.read()
            f.write(content)
        
        # Valida se é um PDF válido
        doc = fitz.open(dest_path)
        page_count = len(doc)
        doc.close()
        
        return {
            "success": True,
            "filename": dest_path.name,
            "page_count": page_count
        }
    except Exception as e:
        # Remove arquivo inválido
        if dest_path.exists():
            dest_path.unlink()
        raise HTTPException(400, f"Arquivo PDF inválido: {str(e)}")


@app.delete("/api/pdfs/{filename}")
def delete_pdf(filename: str):
    """Deleta um PDF."""
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        file_path.unlink()
        # Limpa cache de preview
        for cache_file in PREVIEW_CACHE.glob(f"{filename}_*"):
            cache_file.unlink()
        return {"success": True, "message": f"PDF '{filename}' deletado"}
    except Exception as e:
        raise HTTPException(500, f"Erro ao deletar: {str(e)}")


@app.get("/api/pdfs/{filename}/preview/{page_number}")
def get_page_preview(filename: str, page_number: int, zoom: float = 1.0):
    """Retorna preview de uma página como imagem PNG."""
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        doc = fitz.open(file_path)
        if page_number < 1 or page_number > len(doc):
            raise HTTPException(400, f"Página {page_number} inválida (1-{len(doc)})")
        
        page = doc[page_number - 1]
        mat = fitz.Matrix(zoom, zoom)
        pix = page.get_pixmap(matrix=mat)
        
        img_bytes = pix.tobytes("png")
        doc.close()
        
        return StreamingResponse(
            io.BytesIO(img_bytes),
            media_type="image/png"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"Erro ao gerar preview: {str(e)}")


@app.get("/api/pdfs/{filename}/previews")
def get_all_previews_base64(filename: str, zoom: float = 0.3):
    """Retorna todas as páginas como base64 (para carregar galeria rápido)."""
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        doc = fitz.open(file_path)
        previews = []
        
        for i, page in enumerate(doc):
            mat = fitz.Matrix(zoom, zoom)
            pix = page.get_pixmap(matrix=mat)
            img_bytes = pix.tobytes("png")
            b64 = base64.b64encode(img_bytes).decode()
            previews.append({
                "page": i + 1,
                "base64": f"data:image/png;base64,{b64}"
            })
        
        doc.close()
        return {"filename": filename, "page_count": len(previews), "previews": previews}
    except Exception as e:
        raise HTTPException(500, f"Erro ao gerar previews: {str(e)}")


@app.post("/api/pdfs/{filename}/pages")
def edit_pages(filename: str, request: EditPageRequest):
    """Edita páginas de um PDF (deletar, inserir branco, inserir de outro PDF)."""
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        doc = fitz.open(file_path)
        
        if request.action == "delete":
            # Deletar página
            if request.page_number < 1 or request.page_number > len(doc):
                raise HTTPException(400, f"Página {request.page_number} inválida")
            doc.delete_page(request.page_number - 1)
            
        elif request.action == "insert_blank":
            # Inserir página em branco antes da página especificada
            if request.page_number < 1 or request.page_number > len(doc) + 1:
                raise HTTPException(400, f"Posição {request.page_number} inválida")
            # Pega dimensões da página anterior ou padrão A4
            if len(doc) > 0:
                ref_page = doc[min(request.page_number - 1, len(doc) - 1)]
                width, height = ref_page.rect.width, ref_page.rect.height
            else:
                width, height = 595, 842  # A4
            doc.insert_page(request.page_number - 1, width=width, height=height)
            
        elif request.action == "insert_from_pdf":
            # Inserir página de outro PDF
            if not request.source_pdf or not request.source_page:
                raise HTTPException(400, "source_pdf e source_page são obrigatórios")
            
            source_path = PDF_DIR / request.source_pdf
            if not source_path.exists():
                raise HTTPException(404, f"PDF fonte '{request.source_pdf}' não encontrado")
            
            source_doc = fitz.open(source_path)
            if request.source_page < 1 or request.source_page > len(source_doc):
                raise HTTPException(400, f"Página fonte {request.source_page} inválida")
            
            # Insere a página do source no destino
            doc.insert_pdf(
                source_doc,
                from_page=request.source_page - 1,
                to_page=request.source_page - 1,
                start_at=request.page_number - 1
            )
            source_doc.close()
        else:
            raise HTTPException(400, f"Ação '{request.action}' não reconhecida")
        
        # Salva o PDF modificado
        temp_path = file_path.with_suffix(".tmp.pdf")
        doc.save(temp_path)
        doc.close()
        
        # Substitui o original
        shutil.move(temp_path, file_path)
        
        # Limpa cache de preview
        for cache_file in PREVIEW_CACHE.glob(f"{filename}_*"):
            cache_file.unlink()
        
        return {
            "success": True,
            "action": request.action,
            "page_number": request.page_number,
            "new_page_count": len(fitz.open(file_path))
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"Erro ao editar PDF: {str(e)}")

@app.post("/api/pdfs/{filename}/process")
def process_pdf_to_database(filename: str):
    """
    Processa um PDF e salva as questões extraídas no banco de dados.
    Extrai ano e etapa do nome do arquivo (ex: pas-2-prova-2007.pdf → etapa=2, ano=2007).
    """
    import re
    from processor import process_pdf
    from db_saver import DatabaseSaver
    
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    # Extrai etapa e ano do nome do arquivo
    # Padrões suportados: pas-2-prova-2007, pas_1_2020, pas1-2019, etc.
    pattern = r"pas[_-]?(\d)[_-].*?(\d{4})"
    match = re.search(pattern, filename.lower())
    
    if not match:
        raise HTTPException(400, f"Não foi possível extrair etapa/ano do nome '{filename}'. Use formato: pas-X-prova-AAAA.pdf")
    
    etapa = int(match.group(1))
    ano = int(match.group(2))
    
    if etapa not in [1, 2, 3]:
        raise HTTPException(400, f"Etapa inválida: {etapa}. Use 1, 2 ou 3.")
    
    if ano < 2000 or ano > 2100:
        raise HTTPException(400, f"Ano inválido: {ano}")
    
    try:
        # 1. Processa o PDF (extrai blocos e questões)
        print(f"🔄 Processando {filename} (PAS {etapa} - {ano})...")
        blocos = process_pdf(str(file_path))
        
        if not blocos:
            raise HTTPException(500, "Nenhum bloco/questão detectado no PDF")
        
        # 2. Salva no banco de dados
        saver = DatabaseSaver()
        saver.salvar_dados(blocos, ano, etapa, filename)
        
        # Conta questões extraídas
        total_questoes = sum(len(b.get('questoes', [])) for b in blocos)
        
        return {
            "success": True,
            "filename": filename,
            "etapa": etapa,
            "ano": ano,
            "blocos_extraidos": len(blocos),
            "questoes_extraidas": total_questoes,
            "message": f"PAS {etapa} ({ano}) importado com sucesso!"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"Erro ao processar PDF: {str(e)}")


@app.post("/api/pdfs/{filename}/reconstruct")
def reconstruct_page(filename: str, request: ReconstructPageRequest):
    """
    Reconstrói uma página do PDF com conteúdo formatado manualmente.
    Útil para corrigir páginas com layout problemático do CEBRASPE.
    """
    from reportlab.lib.pagesizes import A4
    from reportlab.lib.units import mm
    from reportlab.pdfgen import canvas as rl_canvas
    from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
    from reportlab.platypus import Paragraph
    from reportlab.lib.enums import TA_JUSTIFY
    
    file_path = PDF_DIR / filename
    if not file_path.exists():
        raise HTTPException(404, f"PDF '{filename}' não encontrado")
    
    try:
        doc = fitz.open(file_path)
        if request.page_number < 1 or request.page_number > len(doc):
            raise HTTPException(400, f"Página {request.page_number} inválida (1-{len(doc)})")
        
        # Pega dimensões da página original
        orig_page = doc[request.page_number - 1]
        width, height = orig_page.rect.width, orig_page.rect.height
        
        # Cria PDF temporário com ReportLab
        temp_pdf_path = PDF_DIR / f"_temp_reconstruct_{filename}"
        c = rl_canvas.Canvas(str(temp_pdf_path), pagesize=(width, height))
        
        # Configurações de layout
        margin_left = 40
        margin_right = width - 40
        margin_top = height - 50
        line_height = 14
        current_y = margin_top
        
        # Estilo de texto
        c.setFont("Helvetica", 10)
        
        # Texto base (contexto do bloco)
        if request.texto_base:
            c.setFont("Helvetica-Oblique", 9)
            # Quebra texto em linhas
            words = request.texto_base.split()
            line = ""
            for word in words:
                test_line = line + " " + word if line else word
                if c.stringWidth(test_line, "Helvetica-Oblique", 9) < (margin_right - margin_left):
                    line = test_line
                else:
                    c.drawString(margin_left, current_y, line)
                    current_y -= line_height
                    line = word
            if line:
                c.drawString(margin_left, current_y, line)
                current_y -= line_height
            
            current_y -= 15  # Espaço após texto base
        
        # Questões
        c.setFont("Helvetica", 10)
        for q in request.questoes:
            # Número da questão em negrito
            c.setFont("Helvetica-Bold", 11)
            c.drawString(margin_left, current_y, f"QUESTÃO {q.numero}")
            current_y -= line_height + 5
            
            # Texto do enunciado
            c.setFont("Helvetica", 10)
            words = q.texto.split()
            line = ""
            for word in words:
                test_line = line + " " + word if line else word
                if c.stringWidth(test_line, "Helvetica", 10) < (margin_right - margin_left):
                    line = test_line
                else:
                    c.drawString(margin_left, current_y, line)
                    current_y -= line_height
                    line = word
            if line:
                c.drawString(margin_left, current_y, line)
                current_y -= line_height
            
            # Imagem (se houver)
            if q.imagem_base64:
                try:
                    # Decodifica base64
                    if "," in q.imagem_base64:
                        img_data = base64.b64decode(q.imagem_base64.split(",")[1])
                    else:
                        img_data = base64.b64decode(q.imagem_base64)
                    
                    # Salva temporariamente
                    temp_img_path = PDF_DIR / f"_temp_img_{q.numero}.png"
                    with open(temp_img_path, "wb") as f:
                        f.write(img_data)
                    
                    # Insere no PDF
                    current_y -= 10
                    img_width = 200
                    img_height = 150
                    if current_y - img_height < 50:
                        c.showPage()
                        current_y = margin_top
                    c.drawImage(str(temp_img_path), margin_left, current_y - img_height, 
                               width=img_width, height=img_height, preserveAspectRatio=True)
                    current_y -= img_height + 10
                    
                    # Remove imagem temporária
                    temp_img_path.unlink()
                except Exception as img_err:
                    print(f"Erro ao processar imagem da questão {q.numero}: {img_err}")
            
            current_y -= 20  # Espaço entre questões
            
            # Se acabou o espaço, nova página
            if current_y < 80:
                c.showPage()
                current_y = margin_top
                c.setFont("Helvetica", 10)
        
        c.save()
        
        # Abre o PDF reconstruído e insere no original
        new_doc = fitz.open(temp_pdf_path)
        
        # Remove a página original
        doc.delete_page(request.page_number - 1)
        
        # Insere as novas páginas
        doc.insert_pdf(new_doc, start_at=request.page_number - 1)
        
        new_doc.close()
        
        # Salva o PDF modificado
        temp_path = file_path.with_suffix(".tmp.pdf")
        doc.save(temp_path)
        doc.close()
        
        # Substitui o original
        shutil.move(temp_path, file_path)
        
        # Limpa arquivos temporários
        if temp_pdf_path.exists():
            temp_pdf_path.unlink()
        
        # Limpa cache de preview
        for cache_file in PREVIEW_CACHE.glob(f"{filename}_*"):
            cache_file.unlink()
        
        return {
            "success": True,
            "page_number": request.page_number,
            "questoes_adicionadas": len(request.questoes),
            "message": f"Página {request.page_number} reconstruída com sucesso!"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"Erro ao reconstruir página: {str(e)}")


@app.get("/health")
def health_check():
    """Health check endpoint."""
    return {"status": "ok", "service": "pdf-management"}


# Para rodar standalone: uvicorn pdf_api:app --host 0.0.0.0 --port 8001
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)

