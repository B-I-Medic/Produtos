CREATE MATERIALIZED VIEW mv_estoque AS
    with ultimo_processamento_estoque as (
        select p.id
        from processamento p
        where p.status = 'CONCLUIDO'
          and p.entidade = 'ESTOQUE'
        order by p.concluido_em desc
        limit 1
    ),
    estoque_interno_tratado as (
        select ei.*, m.cod_ibge, m.descricao as municipio
        from estoque_interno ei
            join empresa_municipio em
                on ei.id_empresa_municipio = em.id
            join municipio m
                on m.id = em.id_municipio
        where ei.processamento = (select id from ultimo_processamento_estoque)
    ),
    estoque_segregado_tratado as (
        select es.*, m.cod_ibge, m.descricao as municipio
        from estoque_segregado es
            join empresa_municipio em
                on es.id_empresa_municipio = em.id
            join municipio m
                on m.id = em.id_municipio
        where es.processamento = (select id from ultimo_processamento_estoque)
    ),
    estoque_vp_tratado as (
        select evp.*, m.cod_ibge, m.descricao as municipio
        from vale_permanente evp
            join empresa_municipio em
                on evp.id_empresa_municipio = em.id
            join municipio m
                on m.id = em.id_municipio
        where evp.processamento = (select id from ultimo_processamento_estoque)
    ),
    estoque_final as (
        select
            coalesce(eit.processamento, est.processamento, evpt.processamento) as processamento,
            coalesce(eit.viman, est.viman, evpt.viman) as viman,
            coalesce(eit.cod_empresa, est.cod_empresa, evpt.cod_empresa) as cod_empresa,
            coalesce(eit.cod_ibge, est.cod_ibge, evpt.cod_ibge) as cod_ibge,
            coalesce(eit.municipio, est.municipio, evpt.municipio) as municipio,
            coalesce(eit.id_empresa_municipio, est.id_empresa_municipio, evpt.id_empresa_municipio) as id_empresa_municipio,
            coalesce(eit.cod_produto, est.cod_produto, evpt.cod_produto) as cod_produto,
            coalesce(eit.qnt_disponivel, 0) as estoque_interno,
            coalesce(est.qnt_disponivel, 0) as estoque_segregado,
            coalesce(evpt.qnt_disponivel, 0) as estoque_vp
        from estoque_interno_tratado eit
        full outer join estoque_segregado_tratado est
            on est.viman = eit.viman
            and est.cod_empresa = eit.cod_empresa
            and est.cod_ibge = eit.cod_ibge
            and est.cod_produto = eit.cod_produto
        full outer join estoque_vp_tratado evpt
            on evpt.viman = eit.viman
            and evpt.cod_empresa = eit.cod_empresa
            and evpt.cod_ibge = eit.cod_ibge
            and evpt.cod_produto = eit.cod_produto
    )
    select
        ef.processamento,
        ef.viman,
        ef.id_empresa_municipio,
        ef.cod_empresa,
        ef.cod_ibge,
        ef.municipio,
        ef.cod_produto,
        sum(ef.estoque_interno) as EstoqueInterno,
        sum(ef.estoque_segregado) as EstoqueSegregado,
        sum(ef.estoque_vp) as EstoqueVP,
        sum(ef.estoque_interno) + sum(ef.estoque_segregado) + sum(ef.estoque_vp) as EstoqueTotal
    from estoque_final ef
    group by processamento, viman, cod_empresa, cod_ibge, municipio, id_empresa_municipio, cod_produto;