ALTER TABLE vale_permanente
    DROP CONSTRAINT pk_vale_permanente;

ALTER TABLE vale_permanente
    ADD COLUMN cod_ibge text,
    ALTER COLUMN id_empresa_municipio drop not null,
    ADD CONSTRAINT pk_vale_permanente PRIMARY KEY (processamento, viman, cod_empresa, cod_ibge, cod_produto),
    ADD CONSTRAINT fk_vale_permanente_cod_ibge
        FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge);

ALTER TABLE estoque_interno
    DROP CONSTRAINT pk_estoque_interno;

ALTER TABLE estoque_interno
    ADD COLUMN cod_ibge text,
    ALTER COLUMN id_empresa_municipio drop not null,
    ADD CONSTRAINT pk_estoque_interno PRIMARY KEY (processamento, viman, cod_empresa, cod_ibge, cod_produto),
    ADD CONSTRAINT fk_estoque_interno_cod_ibge FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge);