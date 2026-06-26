CREATE TABLE "usuario" (
    "id" uuid PRIMARY KEY,
    "nome" text NOT NULL,
    "email" text NOT NULL,
    "senha" text NOT NULL,
    "role" text NOT NULL,
    "ativo" bool NOT NULL,
    "primeiro_acesso" boolean NOT NULL,
    "criado_por" uuid NOT NULL,
    "atualizado_por" uuid,
    "criado_em" timestamptz NOT NULL,
    "atualizado_em" timestamptz
);

CREATE TABLE "password_reset_code" (
    "id" uuid PRIMARY KEY,
    "email" text NOT NULL,
    "codigo" text NOT NULL,
    "expira_em" timestamptz NOT NULL,
    "usado" boolean NOT NULL,
    "criado_em" timestamptz NOT NULL
);

CREATE TABLE "periodo" (
    "id" uuid PRIMARY KEY,
    "descricao" text NOT NULL,
    "data_inicial" date NOT NULL,
    "data_final" date NOT NULL,
    "data_inicial_viman" varchar(8) NOT NULL,
    "data_final_viman" varchar(8) NOT NULL,
    "atualizado_por" uuid NOT NULL,
    "atualizado_em" timestamptz NOT NULL
);

CREATE TABLE "taxa" (
    "id" uuid PRIMARY KEY,
    "descricao" text NOT NULL,
    "taxa" numeric NOT NULL,
    "atualizado_por" uuid NOT NULL,
    "atualizado_em" timestamptz NOT NULL
);

CREATE TABLE "centro_distribuicao" (
    "id" uuid PRIMARY KEY,
    "descricao" text NOT NULL,
    "criado_por" uuid NOT NULL,
    "atualizado_por" uuid,
    "criado_em" timestamptz NOT NULL,
    "atualizado_em" timestamptz
);

CREATE TABLE "municipio" (
    "id" uuid PRIMARY KEY,
    "descricao" text NOT NULL,
    "cod_ibge" text NOT NULL,
    "estado" text NOT NULL
);

CREATE TABLE "empresa" (
    "id" uuid PRIMARY KEY,
    "descricao" text NOT NULL,
    "viman" text NOT NULL,
    "codigo_empresa" text NOT NULL,
    "possui_estoque_interno" boolean NOT NULL,
    "possui_estoque_segregado" boolean NOT NULL,
    "possui_vp" boolean NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL,
    "atualizado_por" uuid,
    "atualizado_em" timestamptz,
    CONSTRAINT "uq_viman_codEmpresa" UNIQUE ("viman", "codigo_empresa")
);

CREATE TABLE "empresa_municipio" (
    "id" uuid PRIMARY KEY,
    "id_empresa" uuid NOT NULL,
    "id_municipio" uuid NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL
);

CREATE TABLE "cd_empresa_munipio" (
    "id" uuid PRIMARY KEY,
    "id_cd" uuid NOT NULL,
    "id_empresa_municipio" uuid NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL
);

CREATE TABLE "estoque_interno_parametros" (
    "id" uuid PRIMARY KEY,
    "id_empresa" uuid NOT NULL,
    "compor_subCd" uuid NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL,
    "atualizado_por" uuid,
    "atualizado_em" timestamptz
);

CREATE TABLE "estoque_segregado_parametros" (
    "id" uuid PRIMARY KEY,
    "id_empresa" uuid NOT NULL,
    "cod_segregado" int NOT NULL,
    "compor_subCd" uuid NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL,
    "atualizado_por" uuid,
    "atualizado_em" timestamptz
);

CREATE TABLE "vale_permanente_parametros" (
    "id" uuid PRIMARY KEY,
    "id_empresa" uuid NOT NULL,
    "cod_vp" int NOT NULL,
    "compor_subCd" uuid NOT NULL,
    "criado_por" uuid NOT NULL,
    "criado_em" timestamptz NOT NULL,
    "atualizado_por" uuid,
    "atualizado_em" timestamptz
);

CREATE TABLE "processamento" (
    "id" uuid PRIMARY KEY,
    "iniciado_em" timestamptz NOT NULL,
    "concluido_em" timestamptz,
    "status" text NOT NULL
);

CREATE TABLE "estoque_interno" (
    "processamento" uuid NOT NULL,
    "viman" text NOT NULL,
    "cod_empresa" text NOT NULL,
    "id_empresa_municipio" uuid NOT NULL,
    "cod_produto" text NOT NULL,
    "qnt_disponivel" int NOT NULL,
    CONSTRAINT "pk_estoque_interno" PRIMARY KEY ("processamento", "viman", "cod_empresa", "id_empresa_municipio", "cod_produto")
);

CREATE TABLE "estoque_segregado" (
    "processamento" uuid NOT NULL,
    "viman" text NOT NULL,
    "cod_empresa" text NOT NULL,
    "id_empresa_municipio" uuid NOT NULL,
    "cod_produto" text NOT NULL,
    "qnt_disponivel" int NOT NULL,
    CONSTRAINT "pk_estoque_segregado" PRIMARY KEY ("processamento", "viman", "cod_empresa", "id_empresa_municipio", "cod_produto")
);

CREATE TABLE "vale_permanente" (
    "processamento" uuid NOT NULL,
    "viman" text NOT NULL,
    "cod_empresa" text NOT NULL,
    "id_empresa_municipio" uuid NOT NULL,
    "cod_produto" text NOT NULL,
    "qnt_disponivel" int NOT NULL,
    CONSTRAINT "pk_vale_permanente" PRIMARY KEY ("processamento", "viman", "cod_empresa", "id_empresa_municipio", "cod_produto")
);

CREATE TABLE "necessidade" (
    "id" uuid PRIMARY KEY,
    "id_empresa_municipio" uuid NOT NULL,
    "cod_produto" text NOT NULL,
    "estoque" int NOT NULL,
    "demanda" int NOT NULL,
    "necessidade" int NOT NULL
);

ALTER TABLE "usuario" ADD FOREIGN KEY ("criado_por") REFERENCES "usuario" ("id");
ALTER TABLE "usuario" ADD FOREIGN KEY ("atualizado_por") REFERENCES "usuario" ("id");
ALTER TABLE "periodo" ADD FOREIGN KEY ("atualizado_por") REFERENCES "usuario" ("id");
ALTER TABLE "taxa" ADD FOREIGN KEY ("atualizado_por") REFERENCES "usuario" ("id");

ALTER TABLE "empresa_municipio" ADD FOREIGN KEY ("id_empresa") REFERENCES "empresa" ("id") ON DELETE CASCADE;
ALTER TABLE "empresa_municipio" ADD FOREIGN KEY ("id_municipio") REFERENCES "municipio" ("id");

ALTER TABLE "cd_empresa_munipio" ADD FOREIGN KEY ("id_cd") REFERENCES "centro_distribuicao" ("id") ON DELETE CASCADE;
ALTER TABLE "cd_empresa_munipio" ADD FOREIGN KEY ("id_empresa_municipio") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;

ALTER TABLE "estoque_interno_parametros" ADD FOREIGN KEY ("id_empresa") REFERENCES "empresa" ("id") ON DELETE CASCADE;
ALTER TABLE "estoque_interno_parametros" ADD FOREIGN KEY ("compor_subCd") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;

ALTER TABLE "estoque_segregado_parametros" ADD FOREIGN KEY ("id_empresa") REFERENCES "empresa" ("id") ON DELETE CASCADE;
ALTER TABLE "estoque_segregado_parametros" ADD FOREIGN KEY ("compor_subCd") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;

ALTER TABLE "vale_permanente_parametros" ADD FOREIGN KEY ("id_empresa") REFERENCES "empresa" ("id") ON DELETE CASCADE;
ALTER TABLE "vale_permanente_parametros" ADD FOREIGN KEY ("compor_subCd") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;

ALTER TABLE "estoque_interno" ADD CONSTRAINT "fk_estoque_interno_empresa_municipio" FOREIGN KEY ("id_empresa_municipio") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;
ALTER TABLE "estoque_interno" ADD CONSTRAINT "fk_estoque_interno_processamento" FOREIGN KEY ("processamento") REFERENCES "processamento" ("id") ON DELETE CASCADE;

ALTER TABLE "estoque_segregado" ADD CONSTRAINT "fk_estoque_segregado_empresa_municipio" FOREIGN KEY ("id_empresa_municipio") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;
ALTER TABLE "estoque_segregado" ADD CONSTRAINT "fk_estoque_segregado_processamento" FOREIGN KEY ("processamento") REFERENCES "processamento" ("id") ON DELETE CASCADE;

ALTER TABLE "vale_permanente" ADD CONSTRAINT "fk_vale_permanente_empresa_municipio" FOREIGN KEY ("id_empresa_municipio") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;
ALTER TABLE "vale_permanente" ADD CONSTRAINT "fk_vale_permanente_processamento" FOREIGN KEY ("processamento") REFERENCES "processamento" ("id") ON DELETE CASCADE;

ALTER TABLE "necessidade" ADD FOREIGN KEY ("id_empresa_municipio") REFERENCES "empresa_municipio" ("id") ON DELETE CASCADE;
