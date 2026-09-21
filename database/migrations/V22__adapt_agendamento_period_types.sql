ALTER TABLE "periodo"
    DROP CONSTRAINT "ck_periodo_configuracao";

UPDATE "periodo"
SET "tipo_periodo" = CASE "tipo_periodo"
    WHEN 'DIAS' THEN 'PROXIMOS_DIAS'
    WHEN 'MESES_FECHADOS' THEN 'PROXIMOS_MESES_FECHADOS'
    ELSE "tipo_periodo"
END
WHERE "descricao" = 'AGENDAMENTO'
  AND "tipo_periodo" IN ('DIAS', 'MESES_FECHADOS');

ALTER TABLE "periodo"
    ADD CONSTRAINT "ck_periodo_configuracao"
        CHECK (
            (
                "tipo_periodo" IS NOT NULL
                AND "tipo_periodo" IN (
                    'DIAS',
                    'MESES_FECHADOS',
                    'PROXIMOS_DIAS',
                    'PROXIMOS_MESES_FECHADOS'
                )
                AND "quantidade" IS NOT NULL
                AND "quantidade" > 0
                AND "data_inicial" IS NULL
                AND "data_final" IS NULL
                AND "data_inicial_viman" IS NULL
                AND "data_final_viman" IS NULL
                AND (
                    (
                        "descricao" = 'AGENDAMENTO'
                        AND "tipo_periodo" IN ('PROXIMOS_DIAS', 'PROXIMOS_MESES_FECHADOS')
                    )
                    OR
                    (
                        "descricao" <> 'AGENDAMENTO'
                        AND "tipo_periodo" IN ('DIAS', 'MESES_FECHADOS')
                    )
                )
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
