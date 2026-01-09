DROP DATABASE IF EXISTS pas_db;
CREATE DATABASE pas_db;
USE pas_db;

-- =======================================================
-- PARTE 1: ACERVO (Conteúdo Estático)
-- =======================================================

-- 1. Tabela PROVAS
CREATE TABLE provas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ano INT NOT NULL,
    etapa INT NOT NULL,
    nome_arquivo_pdf VARCHAR(255),
    titulo VARCHAR(255),
    origem VARCHAR(50) DEFAULT 'PDF_INGESTAO',
    CONSTRAINT uk_prova UNIQUE (ano, etapa)
);

-- 2. Tabela BLOCOS
CREATE TABLE blocos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    texto_base LONGTEXT,
    caminho_imagem VARCHAR(255),
    prova_id BIGINT NOT NULL,
    disciplina VARCHAR(50) DEFAULT 'GERAL',
    FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
);

-- 3. Tabela QUESTOES
CREATE TABLE questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    enunciado LONGTEXT,
    alternativas LONGTEXT, -- JSON
    status VARCHAR(20) DEFAULT 'PENDENTE',
    tipo VARCHAR(10) DEFAULT 'A',
    gabarito VARCHAR(50),
    tags VARCHAR(255),
    bloco_id BIGINT NOT NULL,
    prova_id BIGINT NOT NULL,

    -- Campos novos para Simulado Oficial
    id_obra_json VARCHAR(100),
    simulado_oficial_id BIGINT,

    FOREIGN KEY (bloco_id) REFERENCES blocos(id) ON DELETE CASCADE,
    FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
);

-- 4. Tabela IMAGENS
CREATE TABLE imagens_questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caminho_arquivo VARCHAR(255),
    tag VARCHAR(20) DEFAULT 'ENUNCIADO',
    questao_id BIGINT NOT NULL,
    FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
);

-- =======================================================
-- PARTE 2: SISTEMA (Dados do Usuário e Aprendizado)
-- =======================================================

-- 5. USUARIOS (ATUALIZADA COM REGIAO)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100),
    matricula VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) DEFAULT 'ESTUDANTE',

    -- NOVO CAMPO ADICIONADO AQUI
    regiao VARCHAR(100),

    -- Campos de Gestão
    ultimo_login DATETIME,
    ativo BOOLEAN DEFAULT TRUE
);

-- 6. TOKEN SENHA
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 7. SIMULADOS
CREATE TABLE simulados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(255),
    data_inicio DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_fim DATETIME,
    nota_final DECIMAL(5,2),
    modo VARCHAR(20) DEFAULT 'APRENDIZADO',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 8. RESOLUCOES
CREATE TABLE resolucoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    simulado_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,
    resposta_aluno VARCHAR(10),
    correta BOOLEAN,
    data_resposta DATETIME DEFAULT CURRENT_TIMESTAMP,
    tempo_segundos BIGINT,
    feedback_usuario VARCHAR(20),
    FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE,
    FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
);

-- 9. REGISTROS DE ERROS
CREATE TABLE registros_erros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,

    -- Diagnóstico
    causa VARCHAR(50),
    status VARCHAR(50),
    temperatura INT DEFAULT 0,

    -- Métricas
    total_erros INT DEFAULT 0,
    acertos_consecutivos INT DEFAULT 0,

    -- IA
    necessita_refatoracao_ia BOOLEAN DEFAULT FALSE,
    ultima_versao_refatorada_ia TEXT,

    -- Datas
    data_ultimo_erro DATETIME,
    data_proxima_revisao DATETIME,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
);

-- =======================================================
-- PARTE 3: ESTRUTURA DO SIMULADO PROCEDURAL (OFICIAL)
-- =======================================================

-- 10. Tabela de Simulado Oficial
CREATE TABLE simulados_oficiais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255),
    etapa INT NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT FALSE
);

-- 11. Controle de Cooldown das Obras
CREATE TABLE controle_uso_obras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    obra_id_json VARCHAR(100) NOT NULL,
    data_uso DATETIME DEFAULT CURRENT_TIMESTAMP,
    simulado_id BIGINT,
    FOREIGN KEY (simulado_id) REFERENCES simulados_oficiais(id) ON DELETE CASCADE
);

-- 12. FK Tardia
ALTER TABLE questoes
ADD CONSTRAINT fk_simulado_oficial
FOREIGN KEY (simulado_oficial_id) REFERENCES simulados_oficiais(id) ON DELETE CASCADE;

-- =======================================================
-- PARTE 4: ADMINISTRAÇÃO E SUPORTE
-- =======================================================

-- 13. REPORTS DE CONTEÚDO
CREATE TABLE report_conteudo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    questao_id BIGINT,
    prova_id BIGINT,

    tipo_erro VARCHAR(50),
    descricao TEXT,

    status VARCHAR(20) DEFAULT 'PENDENTE',
    data_report DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE,
    FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
);

-- 14. BUGS TÉCNICOS
CREATE TABLE bug_tracker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,

    categoria VARCHAR(50),
    titulo VARCHAR(255),
    descricao TEXT,

    url_origem VARCHAR(255),
    user_agent VARCHAR(255),
    resolucao_tela VARCHAR(50),

    resolvido BOOLEAN DEFAULT FALSE,
    data_report DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 15. COMUNICADOS E FEED
CREATE TABLE comunicado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255),
    mensagem TEXT,
    tipo VARCHAR(20) DEFAULT 'INFO',
    ativo BOOLEAN DEFAULT TRUE,
    data_envio DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 16. WIKI_POST
CREATE TABLE wiki_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255),
    conteudo LONGTEXT,
    disciplina VARCHAR(50),
    topico VARCHAR(100),
    etapa INT,
    autor_id BIGINT,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    curtidas INT DEFAULT 0,
    FOREIGN KEY (autor_id) REFERENCES usuarios(id) ON DELETE SET NULL
);