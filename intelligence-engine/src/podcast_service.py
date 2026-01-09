"""
Serviço de geração de podcasts a partir de PDFs usando Podcastfy.
Plataforma Darcy - Feature Podcastfy
"""

from podcastfy.client import generate_podcast
import tempfile
import os
from typing import Optional, Dict, Any


class PodcastService:
    """
    Serviço para gerar podcasts educativos a partir de documentos PDF.
    Utiliza a biblioteca Podcastfy para conversão de texto em áudio.
    """
    
    DEFAULT_CONFIG = {
        "conversation_style": ["educational", "engaging", "friendly"],
        "roles_person1": "professor",
        "roles_person2": "estudante curioso", 
        "dialogue_structure": [
            "introduction",
            "main_discussion", 
            "key_takeaways",
            "conclusion"
        ],
        "output_language": "Portuguese",
        "creativity": 0.7,
        "text_instructions": """
            Você está criando um podcast educativo para estudantes do PAS (Programa de Avaliação Seriada) da UnB.
            O conteúdo deve ser didático, envolvente e ajudar na preparação para o vestibular.
            Use linguagem acessível mas mantenha o rigor acadêmico.
            Destaque os pontos mais importantes que podem cair na prova.
        """
    }

    def __init__(self, custom_config: Optional[Dict[str, Any]] = None):
        """
        Inicializa o serviço com configurações opcionais.
        
        Args:
            custom_config: Configurações customizadas para sobrescrever os padrões
        """
        self.config = {**self.DEFAULT_CONFIG}
        if custom_config:
            self.config.update(custom_config)
    
    def generate_from_pdf(self, pdf_path: str, max_pages: int = 30) -> str:
        """
        Gera um podcast a partir de um arquivo PDF.
        
        Args:
            pdf_path: Caminho para o arquivo PDF
            max_pages: Número máximo de páginas a processar (default: 30)
            
        Returns:
            Caminho para o arquivo de áudio gerado (MP3)
            
        Raises:
            ValueError: Se o arquivo não existir ou não for PDF
            RuntimeError: Se houver erro na geração do podcast
        """
        if not os.path.exists(pdf_path):
            raise ValueError(f"Arquivo não encontrado: {pdf_path}")
        
        if not pdf_path.lower().endswith('.pdf'):
            raise ValueError("O arquivo deve ser um PDF")
        
        try:
            # Gera o podcast usando Podcastfy
            audio_file = generate_podcast(
                urls=[pdf_path],
                conversation_config=self.config
            )
            
            return audio_file
            
        except Exception as e:
            raise RuntimeError(f"Erro ao gerar podcast: {str(e)}")
    
    def generate_from_text(self, text: str) -> str:
        """
        Gera um podcast a partir de texto puro.
        
        Args:
            text: Texto para converter em podcast
            
        Returns:
            Caminho para o arquivo de áudio gerado (MP3)
        """
        # Salva o texto em arquivo temporário
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt', encoding='utf-8') as tmp:
            tmp.write(text)
            text_path = tmp.name
        
        try:
            audio_file = generate_podcast(
                urls=[text_path],
                conversation_config=self.config
            )
            return audio_file
        finally:
            # Limpa arquivo temporário
            if os.path.exists(text_path):
                os.unlink(text_path)


# Singleton para uso na API
_service_instance: Optional[PodcastService] = None

def get_podcast_service() -> PodcastService:
    """Retorna instância singleton do serviço."""
    global _service_instance
    if _service_instance is None:
        _service_instance = PodcastService()
    return _service_instance
