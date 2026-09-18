CREATE TABLE anvisa_atualizacao (
    id uuid PRIMARY KEY,
    data_referencia date NOT NULL UNIQUE,
    status text NOT NULL,
    tentativas integer NOT NULL DEFAULT 0,
    solicitado_por uuid,
    solicitado_em timestamptz NOT NULL,
    iniciado_em timestamptz,
    concluido_em timestamptz,
    ultima_falha_em timestamptz,
    ultimo_erro text,
    processamento_id uuid,
    lease_token uuid,
    lease_expira_em timestamptz,
    CONSTRAINT fk_anvisa_atualizacao_solicitado_por
        FOREIGN KEY (solicitado_por) REFERENCES usuario (id),
    CONSTRAINT fk_anvisa_atualizacao_processamento
        FOREIGN KEY (processamento_id) REFERENCES processamento (id) ON DELETE SET NULL,
    CONSTRAINT ck_anvisa_atualizacao_status
        CHECK (status IN ('SOLICITADA', 'EM_EXECUCAO', 'CONCLUIDA', 'FALHOU')),
    CONSTRAINT ck_anvisa_atualizacao_tentativas
        CHECK (tentativas >= 0)
);

CREATE INDEX ix_anvisa_atualizacao_worker
    ON anvisa_atualizacao (status, lease_expira_em, solicitado_em);

CREATE TABLE anvisa_configuracao (
    id smallint PRIMARY KEY,
    retry_cooldown_minutos integer NOT NULL,
    atualizado_por uuid,
    atualizado_em timestamptz,
    CONSTRAINT ck_anvisa_configuracao_id CHECK (id = 1),
    CONSTRAINT ck_anvisa_configuracao_cooldown CHECK (retry_cooldown_minutos BETWEEN 1 AND 1440),
    CONSTRAINT fk_anvisa_configuracao_atualizado_por
        FOREIGN KEY (atualizado_por) REFERENCES usuario (id)
);

INSERT INTO anvisa_configuracao (id, retry_cooldown_minutos)
VALUES (1, 5);

DELETE FROM config_schedule
 WHERE job = 'ATUALIZAR_ANVISA';
