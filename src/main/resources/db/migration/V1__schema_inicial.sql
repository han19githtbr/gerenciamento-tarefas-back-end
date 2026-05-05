-- =============================================
-- V1 — Schema inicial do Gerenciamento de Tarefas
-- Gerado a partir do estado atual do banco (Spring Boot 2.5.4 / Hibernate 5)
-- Este script representa o estado das tabelas JA EXISTENTES no Render.
-- O Flyway usa baseline-on-migrate=true para reconhecer que o banco
-- ja existe e marcar este script como aplicado sem executar novamente.
-- =============================================

-- Departamento
CREATE TABLE IF NOT EXISTS departamento (
    id                  BIGSERIAL PRIMARY KEY,
    titulo              VARCHAR(255),
    ordem_apresentacao  BIGINT
);

-- Pessoa
CREATE TABLE IF NOT EXISTS pessoa (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(255),
    email               VARCHAR(255) UNIQUE,
    id_departamento     BIGINT REFERENCES departamento(id),
    ordem_apresentacao  BIGINT
);

CREATE INDEX IF NOT EXISTS idx_pessoa_email ON pessoa(email);
CREATE INDEX IF NOT EXISTS idx_pessoa_nome  ON pessoa(nome);

-- Tarefa
CREATE TABLE IF NOT EXISTS tarefa (
    id                              BIGSERIAL PRIMARY KEY,
    titulo                          VARCHAR(255),
    descricao                       TEXT,
    prazo                           DATE,
    id_departamento                 BIGINT REFERENCES departamento(id),
    id_pessoa                       BIGINT REFERENCES pessoa(id),
    finalizado                      BOOLEAN NOT NULL DEFAULT FALSE,
    em_andamento                    BOOLEAN NOT NULL DEFAULT FALSE,
    notificacao_vencimento_enviada  BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao                    TIMESTAMP,
    data_conclusao                  DATE,
    ordem_apresentacao              BIGINT
);

-- Mensagem (duvidas do usuario sobre uma tarefa)
CREATE TABLE IF NOT EXISTS mensagem (
    id               BIGSERIAL PRIMARY KEY,
    id_tarefa        BIGINT REFERENCES tarefa(id),
    remetente_email  VARCHAR(255),
    texto            TEXT,
    resposta         TEXT,
    admin_email      VARCHAR(255),
    data_criacao     TIMESTAMP,
    data_resposta    TIMESTAMP,
    respondida       BOOLEAN NOT NULL DEFAULT FALSE
);

-- Notificacao
CREATE TABLE IF NOT EXISTS notificacao (
    id                  BIGSERIAL PRIMARY KEY,
    destinatario_email  VARCHAR(255),
    tarefa_id           BIGINT,
    mensagem            TEXT,
    lida                BOOLEAN NOT NULL DEFAULT FALSE,
    tipo                VARCHAR(255),
    data_criacao        TIMESTAMP
);

-- Tarefa_Alocacao (relacao N:N entre tarefa e pessoa)
CREATE TABLE IF NOT EXISTS tarefa_alocacao (
    id             BIGSERIAL PRIMARY KEY,
    id_tarefa      BIGINT NOT NULL REFERENCES tarefa(id),
    id_pessoa      BIGINT NOT NULL REFERENCES pessoa(id),
    data_alocacao  TIMESTAMP,
    CONSTRAINT uq_tarefa_pessoa UNIQUE (id_tarefa, id_pessoa)
);
