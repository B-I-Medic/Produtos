CREATE OR REPLACE FUNCTION surgilog_sp_centro_distribuicao_id()
RETURNS uuid
LANGUAGE sql
STABLE
AS $$
    SELECT id
    FROM centro_distribuicao
    WHERE descricao = 'Surgilog SP'
$$;

ALTER TABLE municipio
    ALTER COLUMN id_centro_distribuicao DROP NOT NULL,
    ALTER COLUMN id_centro_distribuicao SET DEFAULT surgilog_sp_centro_distribuicao_id();
