CREATE TABLE anvisa_carga (
    id uuid PRIMARY KEY,
    processamento_id uuid NOT NULL,
    criado_em timestamptz NOT NULL,
    ativa boolean NOT NULL,
    produtos_count integer NOT NULL DEFAULT 0,
    modelos_count integer NOT NULL DEFAULT 0,
    CONSTRAINT fk_anvisa_carga_processamento
        FOREIGN KEY (processamento_id) REFERENCES processamento (id)
);

CREATE UNIQUE INDEX uq_anvisa_carga_ativa
    ON anvisa_carga (ativa)
    WHERE ativa = true;

CREATE TABLE anvisa_produto (
    carga_id uuid NOT NULL,
    numero_registro_cadastro text NOT NULL,
    numero_processo text,
    nome_tecnico text,
    classe_risco text,
    nome_comercial text,
    cnpj_detentor_registro_cadastro text,
    detentor_registro_cadastro text,
    nome_fabricante text,
    nome_pais_fabric text,
    dt_pub_registro_cadastro text,
    validade_registro_cadastro text,
    dt_atualizacao_dado text,
    CONSTRAINT pk_anvisa_produto PRIMARY KEY (carga_id, numero_registro_cadastro),
    CONSTRAINT fk_anvisa_produto_carga
        FOREIGN KEY (carga_id) REFERENCES anvisa_carga (id) ON DELETE CASCADE
);

CREATE TABLE anvisa_modelo (
    carga_id uuid NOT NULL,
    numero_registro_cadastro text NOT NULL,
    modelo_ordem integer NOT NULL,
    nome_tecnico text,
    classe_risco text,
    nome_comercial text,
    detentor_registro_cadastro text,
    nome_fabricante text,
    nome_pais_fabric text,
    ds_modelo_produto_medico text NOT NULL,
    modelo_chave text,
    dt_pub_registro_cadastro text,
    validade_registro_cadastro text,
    dt_atualizacao_dado text,
    CONSTRAINT pk_anvisa_modelo PRIMARY KEY (carga_id, numero_registro_cadastro, modelo_ordem),
    CONSTRAINT fk_anvisa_modelo_carga
        FOREIGN KEY (carga_id) REFERENCES anvisa_carga (id) ON DELETE CASCADE
);

CREATE INDEX ix_anvisa_produto_registro
    ON anvisa_produto (numero_registro_cadastro, carga_id);

CREATE INDEX ix_anvisa_modelo_registro
    ON anvisa_modelo (numero_registro_cadastro, carga_id);

CREATE INDEX ix_anvisa_modelo_chave
    ON anvisa_modelo (carga_id, modelo_chave);
