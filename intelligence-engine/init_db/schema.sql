DROP DATABASE IF EXISTS pas_db;
CREATE DATABASE pas_db;
USE pas_db;

-- =======================================================
-- PARTE 1: ACERVO (O que você mandou - Conteúdo)
-- =======================================================

-- 1. Tabela PROVAS
CREATE TABLE provas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ano INT NOT NULL,
    etapa INT NOT NULL,
    nome_arquivo_pdf VARCHAR(255),
    CONSTRAINT uk_prova UNIQUE (ano, etapa)
);

-- 2. Tabela BLOCOS (Textos de Apoio)
CREATE TABLE blocos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    texto_base LONGTEXT, -- Mudei para LONGTEXT para garantir textos grandes
    caminho_imagem VARCHAR(255),
    prova_id BIGINT NOT NULL,
    FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
);

-- 3. Tabela QUESTOES
CREATE TABLE questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    enunciado LONGTEXT, -- Mudei para LONGTEXT
    alternativas LONGTEXT, -- JSON
    status VARCHAR(20) DEFAULT 'PENDENTE',
    tipo VARCHAR(10) DEFAULT 'A',
    gabarito VARCHAR(50),
    tags VARCHAR(255),
    bloco_id BIGINT NOT NULL,
    prova_id BIGINT NOT NULL,
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
-- PARTE 2: SISTEMA (O que falta para funcionar)
-- =======================================================

-- 5. USUARIOS (Essencial para o Login que já existe no Java)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100),
    matricula VARCHAR(50) UNIQUE NOT NULL, -- Adicionei matrícula pois seu Java usa
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) DEFAULT 'ESTUDANTE'
);

-- 6. TOKEN SENHA (Para o "Esqueci minha senha" funcionar)
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 7. SIMULADOS (A sessão de prova)
CREATE TABLE simulados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(255), -- Ex: "Treino de História - PAS 1"
    data_inicio DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_fim DATETIME,
    nota_final DECIMAL(5,2),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 8. RESOLUCOES (As respostas do aluno)
CREATE TABLE resolucoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    simulado_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,
    resposta_aluno VARCHAR(10),
    correta BOOLEAN,
    data_resposta DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE,
    FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
);