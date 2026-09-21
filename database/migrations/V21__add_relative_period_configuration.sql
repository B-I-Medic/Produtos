ALTER TABLE "periodo"
    ADD COLUMN "tipo_periodo" text,
    ADD COLUMN "quantidade" integer;

ALTER TABLE "periodo"
    ALTER COLUMN "data_inicial" DROP NOT NULL,
    ALTER COLUMN "data_final" DROP NOT NULL,
    ALTER COLUMN "data_inicial_viman" DROP NOT NULL,
    ALTER COLUMN "data_final_viman" DROP NOT NULL;

ALTER TABLE "periodo"
    ADD CONSTRAINT "ck_periodo_configuracao"
        CHECK (
            (
                "tipo_periodo" IS NOT NULL
                AND "tipo_periodo" IN ('DIAS', 'MESES_FECHADOS')
                AND "quantidade" IS NOT NULL
                AND "quantidade" > 0
                AND "data_inicial" IS NULL
                AND "data_final" IS NULL
                AND "data_inicial_viman" IS NULL
                AND "data_final_viman" IS NULL
            )
            OR
            (
                "tipo_periodo" IS NULL
                AND "quantidade" IS NULL
                AND "data_inicial" IS NOT NULL
                AND "data_final" IS NOT NULL
                AND "data_inicial_viman" IS NOT NULL
                AND "data_final_viman" IS NOT NULL
            )
        );
