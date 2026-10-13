ALTER TABLE estoque_interno_parametros
    DROP COLUMN id_empresa_municipio;

ALTER TABLE vale_permanente_parametros
    DROP COLUMN id_empresa_municipio;

DROP TABLE cd_empresa_municipio;
DROP TABLE empresa_municipio;
