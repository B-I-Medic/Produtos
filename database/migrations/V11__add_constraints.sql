alter table centro_distribuicao add constraint uq_cd_descricao unique (descricao);

alter table estoque_interno_parametros add constraint uq_estoque_interno_parametros_empresa unique (id_empresa);

alter table estoque_segregado_parametros add constraint uq_estoque_segregado_parametros_empresa_codSegregado unique (id_empresa, cod_segregado);

alter table vale_permanente_parametros add constraint uq_vale_permanente_parametros_empresa_codVp unique (id_empresa, cod_vp);