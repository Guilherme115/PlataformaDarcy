"""
Testes Unitários para o PasExamProcessor

Testa a lógica de negócio de:
- Validação de sequência de questões
- Detecção de ciclos de línguas (Espanhol → Francês → Inglês)
- Limpeza de texto
- Regex de detecção de questões e alternativas
"""
import sys
import os
import pytest
import re

# Adiciona o diretório src ao path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from processor import PasExamProcessor, Questao, BlocoConteudo


class TestRegexPatterns:
    """Testa os padrões regex usados para detectar questões e alternativas."""
    
    @pytest.fixture
    def processor(self, tmp_path):
        """Cria um processor com path temporário."""
        fake_pdf = tmp_path / "fake.pdf"
        fake_pdf.touch()
        return PasExamProcessor(str(fake_pdf), str(tmp_path))
    
    def test_regex_questao_com_palavra_questao(self, processor):
        """Testa detecção de 'QUESTÃO 1'"""
        match = processor.re_inicio_questao.match("QUESTÃO 1")
        assert match is not None
        assert match.group(2) == "1"
        assert match.group(1) == "QUESTÃO "
    
    def test_regex_questao_sem_palavra_questao(self, processor):
        """Testa detecção de apenas número '1.'"""
        match = processor.re_inicio_questao.match("1.")
        assert match is not None
        assert match.group(2) == "1"
        assert match.group(1) is None
    
    def test_regex_questao_com_parentese(self, processor):
        """Testa detecção de '15)'"""
        match = processor.re_inicio_questao.match("15)")
        assert match is not None
        assert match.group(2) == "15"
    
    def test_regex_questao_numero_tres_digitos(self, processor):
        """Testa detecção de número com 3 dígitos '120'"""
        match = processor.re_inicio_questao.match("120")
        assert match is not None
        assert match.group(2) == "120"
    
    def test_regex_questao_nao_detecta_numero_no_meio(self, processor):
        """Não deve detectar número no meio do texto."""
        match = processor.re_inicio_questao.match("O valor é 15 reais")
        assert match is None
    
    def test_regex_alternativa_valida(self, processor):
        """Testa detecção de alternativa 'A texto da alternativa'"""
        match = processor.re_alternativa.match("A Texto da alternativa aqui")
        assert match is not None
        assert match.group(1) == "A"
        assert match.group(2) == "Texto da alternativa aqui"
    
    def test_regex_alternativa_todas_letras(self, processor):
        """Testa todas as letras válidas A, B, C, D"""
        for letra in ["A", "B", "C", "D"]:
            match = processor.re_alternativa.match(f"{letra} Texto")
            assert match is not None, f"Deve detectar alternativa {letra}"
    
    def test_regex_alternativa_nao_detecta_e(self, processor):
        """Não deve detectar letra E como alternativa (PAS usa só A-D)"""
        match = processor.re_alternativa.match("E Texto")
        assert match is None


class TestValidacaoSequencia:
    """Testa a lógica de validação de sequência de questões."""
    
    @pytest.fixture
    def processor(self, tmp_path):
        """Cria um processor limpo."""
        fake_pdf = tmp_path / "fake.pdf"
        fake_pdf.touch()
        return PasExamProcessor(str(fake_pdf), str(tmp_path))
    
    def test_primeira_questao_inicio_prova(self, processor):
        """Questão 1 no início da prova deve ser aceita."""
        assert processor.ultimo_numero_detectado == 0
        assert processor._validar_sequencia(1, tem_palavra_chave=False) == True
    
    def test_sequencia_continua_valida(self, processor):
        """Sequência 1, 2, 3 deve ser aceita."""
        processor.ultimo_numero_detectado = 1
        assert processor._validar_sequencia(2, tem_palavra_chave=False) == True
        
        processor.ultimo_numero_detectado = 2
        assert processor._validar_sequencia(3, tem_palavra_chave=False) == True
    
    def test_sequencia_pulo_rejeitada(self, processor):
        """Pular de 1 para 5 sem palavra-chave deve ser rejeitado."""
        processor.ultimo_numero_detectado = 1
        assert processor._validar_sequencia(5, tem_palavra_chave=False) == False
    
    def test_pulo_com_palavra_chave_aceito(self, processor):
        """Pular com palavra-chave 'QUESTÃO' deve ser aceito (até 5 posições)."""
        processor.ultimo_numero_detectado = 1
        assert processor._validar_sequencia(3, tem_palavra_chave=True) == True
    
    def test_reset_ciclo_linguas(self, processor):
        """Questão 1 após 5+ deve resetar ciclo de línguas."""
        processor.ultimo_numero_detectado = 10
        processor.em_secao_linguas = True
        processor.contador_secao_linguas = 1
        
        assert processor._validar_sequencia(1, tem_palavra_chave=False) == True
    
    def test_bloqueio_questao_11_antes_ciclo_3(self, processor):
        """Questão 11 deve ser bloqueada nos ciclos 1 e 2 (falso positivo)."""
        processor.ultimo_numero_detectado = 10
        processor.em_secao_linguas = True
        processor.contador_secao_linguas = 1
        
        assert processor._validar_sequencia(11, tem_palavra_chave=False) == False
    
    def test_liberacao_questao_11_apos_ciclo_3(self, processor):
        """Questão 11 deve ser liberada após completar 3 ciclos."""
        processor.ultimo_numero_detectado = 10
        processor.em_secao_linguas = True
        processor.contador_secao_linguas = 3
        
        result = processor._validar_sequencia(11, tem_palavra_chave=False)
        assert result == True
        # Deve ter saído da seção de línguas
        assert processor.em_secao_linguas == False
    
    def test_parte_geral_sequencia_normal(self, processor):
        """Parte geral aceita sequência normal."""
        processor.em_secao_linguas = False
        processor.ultimo_numero_detectado = 50
        
        assert processor._validar_sequencia(51, tem_palavra_chave=False) == True
    
    def test_rejeita_numero_maior_que_150(self, processor):
        """Números maiores que 150 são rejeitados (limite de segurança)."""
        processor.em_secao_linguas = False
        processor.ultimo_numero_detectado = 149
        
        assert processor._validar_sequencia(151, tem_palavra_chave=False) == False


class TestLimpezaTexto:
    """Testa a função de limpeza de texto."""
    
    @pytest.fixture
    def processor(self, tmp_path):
        fake_pdf = tmp_path / "fake.pdf"
        fake_pdf.touch()
        return PasExamProcessor(str(fake_pdf), str(tmp_path))
    
    def test_remove_espacos_multiplos(self, processor):
        """Remove múltiplos espaços."""
        texto = "Texto   com    muitos     espaços"
        assert processor._limpar_texto(texto) == "Texto com muitos espaços"
    
    def test_remove_quebras_de_linha(self, processor):
        """Remove quebras de linha extras."""
        texto = "Linha 1\n\n\nLinha 2"
        assert processor._limpar_texto(texto) == "Linha 1 Linha 2"
    
    def test_trim_bordas(self, processor):
        """Remove espaços nas bordas."""
        texto = "   texto com espacos nas bordas   "
        assert processor._limpar_texto(texto) == "texto com espacos nas bordas"


class TestDataclasses:
    """Testa as dataclasses Questao e BlocoConteudo."""
    
    def test_questao_valores_padrao(self):
        """Questao deve ter valores padrão corretos."""
        q = Questao(numero=1)
        assert q.numero == 1
        assert q.texto_enunciado == ""
        assert q.tipo_visualizacao == "TEXTO"
        assert q.caminho_imagem is None
        assert q.alternativas == []
    
    def test_questao_com_alternativas(self):
        """Questao deve armazenar alternativas."""
        q = Questao(numero=1)
        q.alternativas.append("A Primeira opção")
        q.alternativas.append("B Segunda opção")
        
        assert len(q.alternativas) == 2
        assert "A Primeira opção" in q.alternativas
    
    def test_bloco_conteudo_valores_padrao(self):
        """BlocoConteudo deve ter valores padrão corretos."""
        b = BlocoConteudo(id_temp="bloco_1")
        assert b.texto_base == ""
        assert b.questoes == []
        assert b.secao_detectada == 1
    
    def test_bloco_com_questoes(self):
        """BlocoConteudo deve armazenar questões."""
        b = BlocoConteudo(id_temp="bloco_1", texto_base="Texto de apoio")
        b.questoes.append(Questao(numero=1, texto_enunciado="Enunciado 1"))
        b.questoes.append(Questao(numero=2, texto_enunciado="Enunciado 2"))
        
        assert len(b.questoes) == 2
        assert b.questoes[0].numero == 1
        assert b.questoes[1].numero == 2


class TestCiclosLinguas:
    """Testa a lógica completa dos 3 ciclos de línguas."""
    
    @pytest.fixture
    def processor(self, tmp_path):
        fake_pdf = tmp_path / "fake.pdf"
        fake_pdf.touch()
        return PasExamProcessor(str(fake_pdf), str(tmp_path))
    
    def test_ciclo_completo_espanhol_frances_ingles(self, processor):
        """Simula o ciclo completo: Espanhol (1-10) → Francês (1-10) → Inglês (1-10)."""
        
        # CICLO 1: ESPANHOL (questões 1-10)
        assert processor.em_secao_linguas == True
        assert processor.contador_secao_linguas == 1
        
        for num in range(1, 11):
            result = processor._validar_sequencia(num, tem_palavra_chave=False)
            assert result == True, f"Questão {num} do Espanhol deve ser aceita"
            processor.ultimo_numero_detectado = num
        
        # CICLO 2: FRANCÊS (reset para 1)
        result = processor._validar_sequencia(1, tem_palavra_chave=False)
        assert result == True, "Questão 1 do Francês deve ser aceita (reset)"
        processor.contador_secao_linguas = 2  # Simula o incremento que acontece no processar()
        processor.ultimo_numero_detectado = 1
        
        for num in range(2, 11):
            result = processor._validar_sequencia(num, tem_palavra_chave=False)
            processor.ultimo_numero_detectado = num
        
        # CICLO 3: INGLÊS (reset para 1)
        result = processor._validar_sequencia(1, tem_palavra_chave=False)
        assert result == True, "Questão 1 do Inglês deve ser aceita (reset)"
        processor.contador_secao_linguas = 3  # Simula o incremento
        processor.ultimo_numero_detectado = 1
        
        for num in range(2, 11):
            result = processor._validar_sequencia(num, tem_palavra_chave=False)
            processor.ultimo_numero_detectado = num
        
        # PARTE GERAL: Questão 11 deve liberar
        result = processor._validar_sequencia(11, tem_palavra_chave=False)
        assert result == True, "Questão 11 deve ser aceita após 3 ciclos"
        assert processor.em_secao_linguas == False, "Deve sair da seção de línguas"


# ============= TESTES DE INTEGRAÇÃO (OPCIONAL) =============

class TestIntegration:
    """Testes que dependem de PDFs reais (pular se não existir)."""
    
    @pytest.fixture
    def sample_pdf_path(self):
        """Retorna path do PDF de exemplo se existir."""
        path = os.path.join(os.path.dirname(__file__), '..', 'input', 'pas-1-prova-2024.pdf')
        if os.path.exists(path):
            return path
        pytest.skip("PDF de exemplo não encontrado. Pule testes de integração.")
    
    def test_processar_pdf_real(self, sample_pdf_path, tmp_path):
        """Testa processamento de PDF real (se disponível)."""
        processor = PasExamProcessor(sample_pdf_path, str(tmp_path))
        result = processor.processar(ignorar_imagens=True, verbose=False)
        
        assert isinstance(result, list)
        if result:  # Se retornou algo
            assert 'questoes' in result[0]
            assert 'texto_base' in result[0]
