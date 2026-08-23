CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE titular (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    titulo_eleitor VARCHAR(12),
    telefone VARCHAR(20),
    data_nascimento DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP
);

CREATE TABLE endereco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titular_id UUID NOT NULL UNIQUE REFERENCES titular(id) ON DELETE CASCADE,
    logradouro VARCHAR(150),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(9)
);

CREATE TABLE consentimento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titular_id UUID NOT NULL REFERENCES titular(id) ON DELETE CASCADE,
    finalidade VARCHAR(300) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    agente_responsavel VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    data_consentimento TIMESTAMP NOT NULL,
    data_revogacao TIMESTAMP
);

CREATE TABLE log_acesso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titular_id UUID NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    acao VARCHAR(30) NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_titular_cpf ON titular(cpf);
CREATE INDEX idx_consentimento_titular ON consentimento(titular_id);
CREATE INDEX idx_log_acesso_titular ON log_acesso(titular_id);
