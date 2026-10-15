CREATE TABLE estoque_segregado_catalogo (
                                                                         id uuid PRIMARY KEY,
                                                                         id_empresa uuid NOT NULL,
                                                                         cod_segregado integer NOT NULL,
                                                                         descricao text,
                                                                         cod_ibge text,
                                                                         ativo boolean NOT NULL DEFAULT false,
                                                                         atualizado_por uuid,
                                                                         atualizado_em timestamptz,
                                                                         CONSTRAINT uq_estoque_segregado_catalogo_empresa_codigo
                                                                             UNIQUE (id_empresa, cod_segregado),
                                                                         CONSTRAINT fk_estoque_segregado_catalogo_empresa
                                                                             FOREIGN KEY (id_empresa) REFERENCES empresa (id) ON DELETE CASCADE,
                                                                         CONSTRAINT fk_estoque_segregado_catalogo_municipio
                                                                             FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge),
                                                                         CONSTRAINT fk_estoque_segregado_catalogo_atualizado_por
                                                                             FOREIGN KEY (atualizado_por) REFERENCES usuario (id),
                                                                         CONSTRAINT ck_estoque_segregado_catalogo_ativo_exige_municipio
                                                                             CHECK (NOT ativo OR cod_ibge IS NOT NULL)
                             );

INSERT INTO estoque_segregado_catalogo (
    id,
    id_empresa,
    cod_segregado,
    cod_ibge,
    ativo,
    atualizado_por,
    atualizado_em
)
SELECT
    esp.id,
    esp.id_empresa,
    esp.cod_segregado,
    esp.cod_ibge,
    true,
    esp.atualizado_por,
    esp.atualizado_em
FROM estoque_segregado_parametros esp;

UPDATE estoque_segregado_catalogo esc
SET ativo = false,
    atualizado_em = now()
FROM empresa e
WHERE e.id = esc.id_empresa
  AND e.possui_estoque_segregado = false;

DROP TABLE estoque_segregado_parametros;

CREATE OR REPLACE FUNCTION inativar_estoque_segregado_catalogo_empresa_desabilitada()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.possui_estoque_segregado IS DISTINCT FROM NEW.possui_estoque_segregado
        AND NEW.possui_estoque_segregado = false THEN
        UPDATE estoque_segregado_catalogo
        SET ativo = false,
            atualizado_por = NEW.atualizado_por,
            atualizado_em = now()
        WHERE id_empresa = NEW.id
          AND ativo = true;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_empresa_inativar_estoque_segregado_catalogo
    AFTER UPDATE OF possui_estoque_segregado ON empresa
    FOR EACH ROW
EXECUTE FUNCTION inativar_estoque_segregado_catalogo_empresa_desabilitada();

CREATE OR REPLACE FUNCTION validar_estoque_segregado_catalogo_empresa_habilitada()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.ativo = true
        AND NOT EXISTS (
            SELECT 1
            FROM empresa
            WHERE id = NEW.id_empresa
              AND possui_estoque_segregado = true
        ) THEN
        RAISE EXCEPTION 'A empresa precisa possuir estoque segregado habilitado para ativar o registro'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_estoque_segregado_catalogo_empresa_habilitada
    BEFORE INSERT OR UPDATE ON estoque_segregado_catalogo
    FOR EACH ROW
EXECUTE FUNCTION validar_estoque_segregado_catalogo_empresa_habilitada();

CREATE INDEX ix_estoque_segregado_catalogo_ativos
    ON estoque_segregado_catalogo (id_empresa, cod_ibge, cod_segregado)
    WHERE ativo = true;

ALTER TABLE config_schedule
    ALTER COLUMN atualizado_por DROP NOT NULL;

INSERT INTO config_schedule (
    id,
    job,
    cron,
    ativo,
    atualizado_por,
    atualizado_em
)
VALUES (
           'a2be4a73-cb9d-4fd8-87f9-bb991517f115',
           'SINCRONIZAR_ESTOQUE_SEGREGADO',
           '0 0 7 * * MON-FRI',
           true,
           NULL,
           now()
       )
ON CONFLICT (job) DO NOTHING;
