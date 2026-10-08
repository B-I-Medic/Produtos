ALTER TABLE municipio
    ADD CONSTRAINT uq_municipio_cod_ibge UNIQUE (cod_ibge);