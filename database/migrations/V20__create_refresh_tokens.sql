CREATE TABLE "refresh_token" (
    "id" uuid PRIMARY KEY,
    "usuario_id" uuid NOT NULL,
    "token_hash" varchar(64) NOT NULL UNIQUE,
    "family_id" uuid NOT NULL,
    "expira_em" timestamptz NOT NULL,
    "criado_em" timestamptz NOT NULL,
    "revogado_em" timestamptz,
    "substituido_por" uuid,
    "ultimo_uso_em" timestamptz,
    CONSTRAINT "fk_refresh_token_usuario"
        FOREIGN KEY ("usuario_id") REFERENCES "usuario" ("id") ON DELETE CASCADE,
    CONSTRAINT "fk_refresh_token_substituido_por"
        FOREIGN KEY ("substituido_por") REFERENCES "refresh_token" ("id")
);

CREATE INDEX "ix_refresh_token_usuario_status"
    ON "refresh_token" ("usuario_id", "revogado_em", "expira_em");

CREATE INDEX "ix_refresh_token_family"
    ON "refresh_token" ("family_id");
