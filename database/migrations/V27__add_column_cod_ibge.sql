ALTER TABLE vale_permanente
    DROP CONSTRAINT pk_vale_permanente;

ALTER TABLE vale_permanente
    ADD COLUMN cod_ibge text,
    ALTER COLUMN id_empresa_municipio drop not null,
    ADD CONSTRAINT fk_vale_permanente_cod_ibge FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge);

UPDATE vale_permanente vp
    SET cod_ibge = (select m.cod_ibge from empresa_municipio em join municipio m on m.id = em.id_municipio where em.id = vp.id_empresa_municipio);

ALTER TABLE vale_permanente
    ALTER COLUMN cod_ibge set not null,
    DROP COLUMN id_empresa_municipio,
    ADD CONSTRAINT pk_vale_permanente PRIMARY KEY (processamento, viman, cod_empresa, cod_ibge, cod_produto);

-- Estoque interno

ALTER TABLE estoque_interno
    DROP CONSTRAINT pk_estoque_interno;

ALTER TABLE estoque_interno
    ADD COLUMN cod_ibge text,
    ALTER COLUMN id_empresa_municipio drop not null,
    ADD CONSTRAINT fk_estoque_interno_cod_ibge FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge);

UPDATE estoque_interno ei
    SET cod_ibge = (select m.cod_ibge from empresa_municipio em join municipio m on m.id = em.id_municipio where em.id = ei.id_empresa_municipio);

ALTER TABLE estoque_interno
    ALTER COLUMN cod_ibge set not null,
    DROP COLUMN id_empresa_municipio,
    ADD CONSTRAINT pk_estoque_interno PRIMARY KEY (processamento, viman, cod_empresa, cod_ibge, cod_produto);

-- Estoque segregado

ALTER TABLE estoque_segregado
    DROP CONSTRAINT pk_estoque_segregado;

ALTER TABLE estoque_segregado
    ADD COLUMN cod_ibge text,
    ALTER COLUMN id_empresa_municipio drop not null,
    ADD CONSTRAINT fk_estoque_segregado_cod_ibge FOREIGN KEY (cod_ibge) REFERENCES municipio (cod_ibge);

UPDATE estoque_segregado es
    SET cod_ibge = (select m.cod_ibge from empresa_municipio em join municipio m on m.id = em.id_municipio where em.id = es.id_empresa_municipio);

ALTER TABLE estoque_segregado
    ALTER COLUMN cod_ibge set not null,
    DROP COLUMN id_empresa_municipio,
    ADD CONSTRAINT pk_estoque_segregado PRIMARY KEY (processamento, viman, cod_empresa, cod_ibge, cod_produto);