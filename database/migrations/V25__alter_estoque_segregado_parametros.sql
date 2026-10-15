alter table estoque_segregado_parametros
    add column cod_ibge text,
    add constraint fk_estoque_segregado_parametros_municipio foreign key (cod_ibge) references municipio (cod_ibge);

update estoque_segregado_parametros esp
set cod_ibge = (select m.cod_ibge from empresa_municipio em join municipio m on m.id = em.id_municipio where em.id = esp.id_empresa_municipio);

alter table estoque_segregado_parametros
    alter column cod_ibge set not null,
    drop column id_empresa_municipio;