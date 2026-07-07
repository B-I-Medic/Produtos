CREATE TABLE produto (
    viman text not null,
    cod_empresa text not null,
    cod_produto text not null,
    descricao text not null,
    marca text not null,
    tipo text not null,
    anvisa integer not null,
    situacao text not null,
    criado_por text not null,
    criad_em timestamptz not null
);

ALTER TABLE processamento
    ADD COLUMN entidade text;

UPDATE processamento
    SET entidade = 'ESTOQUE_INTERNO';

ALTER TABLE processamento
    ALTER COLUMN entidade SET NOT NULL;

ALTER TABLE produto ADD CONSTRAINT pk_produto PRIMARY KEY (viman, cod_empresa, cod_produto);