alter table "cd_empresa_munipio" rename to "cd_empresa_municipio";

ALTER TABLE "cd_empresa_municipio" ADD CONSTRAINT "uq_cd_empresa_municipio" UNIQUE ("id_empresa_municipio");

