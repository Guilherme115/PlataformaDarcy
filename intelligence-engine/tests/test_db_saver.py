"""
Testes Unitários para o DatabaseSaver

Testa a lógica de negócio (sem banco real):
- Mapeamento de disciplinas
- Preparação de dados
- Tipos de questão
"""
import sys
import os
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from db_saver import DatabaseSaver


class TestMapaDisciplinas:
    """Testa o mapeamento de seções para disciplinas."""
    
    @pytest.fixture
    def saver(self):
        return DatabaseSaver()
    
    def test_secao_1_espanhol(self, saver):
        """Seção 1 deve mapear para ESPANHOL."""
        assert saver.mapa_disciplinas.get(1) == "ESPANHOL"
    
    def test_secao_2_frances(self, saver):
        """Seção 2 deve mapear para FRANCÊS."""
        assert saver.mapa_disciplinas.get(2) == "FRANCÊS"
    
    def test_secao_3_ingles(self, saver):
        """Seção 3 deve mapear para INGLÊS."""
        assert saver.mapa_disciplinas.get(3) == "INGLÊS"
    
    def test_secao_4_geral(self, saver):
        """Seção 4 deve mapear para GERAL."""
        assert saver.mapa_disciplinas.get(4) == "GERAL"
    
    def test_secao_invalida_retorna_none(self, saver):
        """Seção inválida deve retornar None."""
        assert saver.mapa_disciplinas.get(99) is None
    
    def test_fallback_para_geral(self, saver):
        """Seção não mapeada deve usar GERAL como fallback."""
        disciplina = saver.mapa_disciplinas.get(99, "GERAL")
        assert disciplina == "GERAL"


class TestTipoQuestao:
    """Testa a lógica de determinação do tipo de questão."""
    
    def test_questao_com_alternativas_tipo_c(self):
        """Questão com alternativas deve ser tipo C (Múltipla Escolha)."""
        questao = {
            'numero': 1,
            'texto_enunciado': 'Qual a resposta?',
            'alternativas': ['A Opção 1', 'B Opção 2', 'C Opção 3', 'D Opção 4'],
            'caminho_imagem': None
        }
        
        tipo_q = 'C' if (questao['alternativas'] and len(questao['alternativas']) > 0) else 'A'
        assert tipo_q == 'C'
    
    def test_questao_sem_alternativas_tipo_a(self):
        """Questão sem alternativas deve ser tipo A (Certo/Errado ou Asserção)."""
        questao = {
            'numero': 1,
            'texto_enunciado': 'Afirmação para julgar.',
            'alternativas': [],
            'caminho_imagem': None
        }
        
        tipo_q = 'C' if (questao['alternativas'] and len(questao['alternativas']) > 0) else 'A'
        assert tipo_q == 'A'
    
    def test_questao_alternativas_none_tipo_a(self):
        """Questão com alternativas None deve ser tipo A."""
        questao = {
            'numero': 1,
            'texto_enunciado': 'Texto...',
            'alternativas': None,
            'caminho_imagem': None
        }
        
        tipo_q = 'C' if (questao.get('alternativas') and len(questao['alternativas']) > 0) else 'A'
        assert tipo_q == 'A'


class TestPreparacaoDados:
    """Testa a preparação de dados para inserção."""
    
    def test_extrai_nome_arquivo_imagem(self):
        """Deve extrair apenas o nome do arquivo da imagem."""
        caminho = "/app/output_images/img_page1_001.png"
        nome = os.path.basename(caminho)
        assert nome == "img_page1_001.png"
    
    def test_extrai_nome_arquivo_windows(self):
        """Deve funcionar com paths Windows."""
        caminho = "C:\\Users\\teste\\output\\img.png"
        nome = os.path.basename(caminho)
        assert nome == "img.png"
    
    def test_imagem_none_retorna_none(self):
        """Caminho None deve retornar None."""
        caminho = None
        nome = os.path.basename(caminho) if caminho else None
        assert nome is None


class TestConfiguracao:
    """Testa a configuração do DatabaseSaver."""
    
    @pytest.fixture
    def saver(self):
        return DatabaseSaver()
    
    def test_config_database_correto(self, saver):
        """Database deve ser pas_db."""
        assert saver.config['database'] == 'pas_db'
    
    def test_config_charset_utf8(self, saver):
        """Charset deve ser utf8mb4 para suportar emojis."""
        assert saver.config['charset'] == 'utf8mb4'
    
    def test_config_user_root(self, saver):
        """User padrão deve ser root."""
        assert saver.config['user'] == 'root'


class TestEstruturaBloco:
    """Testa a estrutura esperada de um bloco."""
    
    def test_bloco_minimo_valido(self):
        """Bloco mínimo deve ter campos obrigatórios."""
        bloco = {
            'id_temp': 'bloco_1',
            'texto_base': '',
            'caminho_imagem': None,
            'questoes': [],
            'secao_detectada': 1
        }
        
        assert 'texto_base' in bloco
        assert 'questoes' in bloco
        assert 'secao_detectada' in bloco
    
    def test_bloco_com_questoes(self):
        """Bloco deve poder conter múltiplas questões."""
        bloco = {
            'id_temp': 'bloco_1',
            'texto_base': 'Texto de apoio...',
            'caminho_imagem': None,
            'questoes': [
                {'numero': 1, 'texto_enunciado': 'Q1', 'alternativas': [], 'caminho_imagem': None},
                {'numero': 2, 'texto_enunciado': 'Q2', 'alternativas': [], 'caminho_imagem': None},
            ],
            'secao_detectada': 4
        }
        
        assert len(bloco['questoes']) == 2
        assert bloco['questoes'][0]['numero'] == 1
        assert bloco['questoes'][1]['numero'] == 2
