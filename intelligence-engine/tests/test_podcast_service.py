"""
Testes Unitários para o PodcastService

Testa a lógica de negócio:
- Configuração padrão
- Merge de configurações customizadas
- Validação de parâmetros
"""
import sys
import os
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from podcast_service import PodcastService


class TestPodcastServiceConfig:
    """Testa a configuração do PodcastService."""
    
    def test_config_padrao_existe(self):
        """Configuração padrão deve existir."""
        service = PodcastService()
        assert service.config is not None
        assert isinstance(service.config, dict)
    
    def test_config_padrao_tem_campos_obrigatorios(self):
        """Configuração padrão deve ter campos obrigatórios."""
        service = PodcastService()
        
        assert "conversation_style" in service.config
        assert "roles_person1" in service.config
        assert "roles_person2" in service.config
        assert "output_language" in service.config
    
    def test_idioma_padrao_portugues(self):
        """Idioma padrão deve ser Portuguese."""
        service = PodcastService()
        assert service.config["output_language"] == "Portuguese"
    
    def test_papel_person1_professor(self):
        """Person1 deve ser professor."""
        service = PodcastService()
        assert service.config["roles_person1"] == "professor"
    
    def test_papel_person2_estudante(self):
        """Person2 deve ser estudante curioso."""
        service = PodcastService()
        assert service.config["roles_person2"] == "estudante curioso"
    
    def test_estilo_conversa_educacional(self):
        """Estilo de conversa deve incluir 'educational'."""
        service = PodcastService()
        assert "educational" in service.config["conversation_style"]
    
    def test_creativity_entre_0_e_1(self):
        """Creativity deve estar entre 0 e 1."""
        service = PodcastService()
        assert 0 <= service.config["creativity"] <= 1


class TestCustomConfig:
    """Testa merge de configurações customizadas."""
    
    def test_custom_config_sobrescreve_padrao(self):
        """Configuração customizada deve sobrescrever padrão."""
        custom = {"output_language": "English"}
        service = PodcastService(custom_config=custom)
        
        assert service.config["output_language"] == "English"
    
    def test_custom_config_mantem_outros_campos(self):
        """Campos não sobrescritos devem manter valores padrão."""
        custom = {"output_language": "English"}
        service = PodcastService(custom_config=custom)
        
        assert service.config["roles_person1"] == "professor"
        assert service.config["creativity"] == 0.7
    
    def test_custom_config_adiciona_novos_campos(self):
        """Campos novos devem ser adicionados."""
        custom = {"novo_campo": "valor_novo"}
        service = PodcastService(custom_config=custom)
        
        assert service.config["novo_campo"] == "valor_novo"
    
    def test_custom_config_none_usa_padrao(self):
        """None como custom_config deve usar apenas padrão."""
        service = PodcastService(custom_config=None)
        
        assert service.config["output_language"] == "Portuguese"


class TestDefaultConfigValues:
    """Testa valores específicos da configuração padrão."""
    
    def test_estrutura_dialogo_completa(self):
        """Estrutura do diálogo deve ter todas as partes."""
        service = PodcastService()
        estrutura = service.config["dialogue_structure"]
        
        assert "introduction" in estrutura
        assert "main_discussion" in estrutura
        assert "key_takeaways" in estrutura
        assert "conclusion" in estrutura
    
    def test_text_instructions_menciona_pas(self):
        """Instruções devem mencionar PAS/UnB."""
        service = PodcastService()
        instrucoes = service.config["text_instructions"]
        
        assert "PAS" in instrucoes
        assert "UnB" in instrucoes
    
    def test_text_instructions_menciona_vestibular(self):
        """Instruções devem mencionar vestibular."""
        service = PodcastService()
        instrucoes = service.config["text_instructions"]
        
        assert "vestibular" in instrucoes


class TestServiceInstantiation:
    """Testa instanciação do serviço."""
    
    def test_instancia_sem_parametros(self):
        """Deve poder instanciar sem parâmetros."""
        service = PodcastService()
        assert service is not None
    
    def test_instancia_com_dict_vazio(self):
        """Deve aceitar dict vazio como config."""
        service = PodcastService(custom_config={})
        assert service.config["output_language"] == "Portuguese"
    
    def test_multiplas_instancias_independentes(self):
        """Múltiplas instâncias devem ser independentes."""
        service1 = PodcastService(custom_config={"creativity": 0.5})
        service2 = PodcastService(custom_config={"creativity": 0.9})
        
        assert service1.config["creativity"] == 0.5
        assert service2.config["creativity"] == 0.9
    
    def test_config_padrao_nao_modificada(self):
        """DEFAULT_CONFIG não deve ser modificada por instâncias."""
        original = PodcastService.DEFAULT_CONFIG.copy()
        
        service = PodcastService(custom_config={"creativity": 0.1})
        
        assert PodcastService.DEFAULT_CONFIG["creativity"] == original["creativity"]
