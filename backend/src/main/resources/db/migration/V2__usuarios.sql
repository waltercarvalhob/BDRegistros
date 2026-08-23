CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login VARCHAR(60) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    nome_completo VARCHAR(150) NOT NULL,
    papel VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT true,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);
