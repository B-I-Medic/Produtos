create table demanda (
    processamento uuid not null,
    cod_empresa text not null,
    cod_ibge text not null,
    cod_produto text not null,
    qnt_orcado int not null,
    qnt_aprovado int not null,
    qnt_agendado int not null,
    qnt_utilizado int not null,
    qnt_total int not null
);

ALTER TABLE demanda ADD PRIMARY KEY (processamento, cod_empresa, cod_ibge, cod_produto);

ALTER TABLE demanda ADD CONSTRAINT fk_demanda_processamento FOREIGN KEY (processamento) REFERENCES processamento (id) ON DELETE CASCADE;

