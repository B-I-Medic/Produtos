ALTER TABLE anvisa_carga DROP CONSTRAINT fk_anvisa_carga_processamento;

ALTER TABLE anvisa_carga ADD CONSTRAINT fk_anvisa_carga_processamento FOREIGN KEY (processamento_id) REFERENCES processamento (id) ON DELETE CASCADE;