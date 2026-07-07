ALTER TABLE processamento
    ADD COLUMN tipo_disparo text;

UPDATE processamento
SET tipo_disparo = 'AUTOMATICO';

ALTER TABLE processamento
    ALTER COLUMN tipo_disparo SET NOT NULL;