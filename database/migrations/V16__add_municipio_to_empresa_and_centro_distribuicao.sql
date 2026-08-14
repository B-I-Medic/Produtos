ALTER TABLE "empresa"
    ADD COLUMN "municipio_id" uuid;

ALTER TABLE "centro_distribuicao"
    ADD COLUMN "municipio_id" uuid;

ALTER TABLE "empresa"
    ADD CONSTRAINT "fk_empresa_municipio_endereco"
        FOREIGN KEY ("municipio_id") REFERENCES "municipio" ("id");

ALTER TABLE "centro_distribuicao"
    ADD CONSTRAINT "fk_cd_municipio_endereco"
        FOREIGN KEY ("municipio_id") REFERENCES "municipio" ("id");
