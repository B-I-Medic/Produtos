ALTER TABLE municipio
    ADD COLUMN id_centro_distribuicao uuid,
    ADD CONSTRAINT uq_municipio_cod_ibge UNIQUE (cod_ibge),
    ADD CONSTRAINT fk_municipio_centro_distribuicao
        FOREIGN KEY (id_centro_distribuicao)
            REFERENCES centro_distribuicao (id)
            ON DELETE SET NULL;

UPDATE municipio
SET id_centro_distribuicao = CASE
    WHEN estado IN (
        'São Paulo',
        'Rio de Janeiro',
        'Minas Gerais',
        'Espírito Santo',
        'Paraná',
        'Santa Catarina',
        'Rio Grande do Sul'
    ) THEN (select id from centro_distribuicao where descricao = 'Surgilog SP')
    WHEN estado IN (
         'Distrito Federal',
         'Goiás',
         'Tocantins',
         'Pará',
         'Amapá'
    ) THEN (select id from centro_distribuicao where descricao = 'Surgilog DF')
    WHEN estado IN (
          'Bahia',
          'Sergipe',
          'Alagoas',
          'Pernambuco',
          'Paraíba',
          'Rio Grande do Norte',
          'Ceará',
          'Piauí',
          'Maranhão'
    ) THEN (select id from centro_distribuicao where descricao = 'Surgilog BA')
    WHEN estado IN (
           'Mato Grosso',
           'Mato Grosso do Sul',
           'Rondônia',
           'Acre',
           'Amazonas',
           'Roraima'
    ) THEN (select id from centro_distribuicao where descricao = 'Surgilog MT')
    ELSE id_centro_distribuicao
    END;

ALTER TABLE municipio
    ALTER COLUMN id_centro_distribuicao SET NOT NULL;