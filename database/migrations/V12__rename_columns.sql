alter table estoque_interno_parametros
    rename column "compor_subCd" to id_empresa_municipio;

alter table estoque_segregado_parametros
    rename column "compor_subCd" to id_empresa_municipio;

alter table vale_permanente_parametros
    rename column "compor_subCd" to id_empresa_municipio;