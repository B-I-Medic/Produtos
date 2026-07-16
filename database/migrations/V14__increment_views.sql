DROP MATERIALIZED VIEW IF EXISTS mv_estoque CASCADE;

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
         select ei.*,
                m.cod_ibge,
                m.estado as estado,
                m.descricao as municipio
         from estoque_interno ei
                  join empresa_municipio em
                       on ei.id_empresa_municipio = em.id
                  join municipio m
                       on m.id = em.id_municipio
         where ei.processamento = (select id from ultimo_processamento_estoque)
     ),
     estoque_segregado_tratado as (
         select es.*,
                m.cod_ibge,
                m.estado as estado,
                m.descricao as municipio
         from estoque_segregado es
                  join empresa_municipio em
                       on es.id_empresa_municipio = em.id
                  join municipio m
                       on m.id = em.id_municipio
         where es.processamento = (select id from ultimo_processamento_estoque)
     ),
     estoque_vp_tratado as (
         select evp.*,
                m.cod_ibge,
                m.estado as estado,
                m.descricao as municipio
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
             coalesce(eit.estado, est.estado, evpt.estado) as estado,
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
    ef.estado,
    ef.municipio,
    ef.cod_produto,
    sum(ef.estoque_interno) as EstoqueInterno,
    sum(ef.estoque_segregado) as EstoqueSegregado,
    sum(ef.estoque_vp) as EstoqueVP,
    sum(ef.estoque_interno) + sum(ef.estoque_segregado) + sum(ef.estoque_vp) as EstoqueTotal
from estoque_final ef
group by processamento, viman, cod_empresa, cod_ibge, estado, municipio, id_empresa_municipio, cod_produto;

create view necessidade_de_compra as
with ultimo_processamento_demanda as (
    select p.id
    from processamento p
    where p.status = 'CONCLUIDO'
      and p.entidade = 'DEMANDA'
    order by p.concluido_em desc
    limit 1
), demanda_tratada as (
    select
        em.id as id_empresa_municipio,
        d.cod_empresa as cod_empresa,
        d.cod_ibge as cod_ibge,
        m.estado as estado,
        m.descricao as municipio,
        d.cod_produto as cod_produto,
        qnt_orcado as qnt_orcado,
        qnt_orcado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO') as qnt_orcado_taxa,
        qnt_aprovado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO_APROVADO') as qnt_aprovado_taxa,
        qnt_agendado * (select t.taxa from taxa t where t.descricao = 'AGENDAMENTO') as qnt_agendado_taxa,
        qnt_utilizado * (select t.taxa from taxa t where t.descricao = 'CIRURGIA') as qnt_utilizado_taxa,
        (qnt_orcado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO') +
         qnt_aprovado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO_APROVADO')) +
        (qnt_agendado * (select t.taxa from taxa t where t.descricao = 'AGENDAMENTO') +
         qnt_utilizado * (select t.taxa from taxa t where t.descricao = 'CIRURGIA')) as demanda_total
    from demanda d
             join empresa e
                  on e.viman = 'UFX'
                      and e.codigo_empresa = d.cod_empresa
             join municipio m
                  on m.cod_ibge = d.cod_ibge
             join empresa_municipio em
                  on em.id_empresa = e.id
                      and em.id_municipio = m.id
    where d.processamento = (select * from ultimo_processamento_demanda)
), estoque_demanda as (
    select
        coalesce(mv.viman, 'UFX') as viman,
        coalesce(mv.id_empresa_municipio, dt.id_empresa_municipio) as id_empresa_municipio,
        coalesce(mv.cod_empresa, dt.cod_empresa) as cod_empresa,
        coalesce(mv.cod_ibge, dt.cod_ibge) as cod_ibge,
        coalesce(mv.estado, dt.estado) as estado,
        coalesce(mv.municipio, dt.municipio) as municipio,
        coalesce(mv.cod_produto, dt.cod_produto) as cod_produto,
        coalesce(mv.estoqueinterno, 0) as estoque_interno,
        coalesce(mv.estoquesegregado, 0) as estoque_segregado,
        coalesce(mv.estoquevp, 0) as estoque_vp,
        coalesce(mv.estoquetotal, 0) as estoque_total,
        coalesce(dt.qnt_orcado_taxa, 0) as demanda_orcado,
        coalesce(dt.qnt_aprovado_taxa, 0) as demanda_aprovado,
        coalesce(dt.qnt_agendado_taxa, 0) as demanda_agendado,
        coalesce(dt.qnt_utilizado_taxa, 0) as demanda_utilizado,
        coalesce(dt.demanda_total, 0) as demanda_total
    from mv_estoque mv
             full outer join demanda_tratada dt
                             on mv.viman = 'UFX'
                                 and mv.cod_empresa = dt.cod_empresa
                                 and mv.cod_ibge = dt.cod_ibge
                                 and mv.cod_produto = dt.cod_produto
    where mv.estoquetotal > 0 or dt.demanda_total > 0
) select
      cd.descricao as centro_distribuicao,
      ed.viman as viman,
      ed.cod_empresa as cod_empresa,
      e.descricao as empresa,
      ed.estado as estado,
      ed.municipio as municipio,
      ed.cod_produto as cod_produto,
      p.descricao as produto,
      p.marca as marca,
      coalesce(p.anvisa, 'Sem anvisa') as anvisa,
      ed.estoque_interno,
      ed.estoque_segregado,
      ed.estoque_vp,
      ed.estoque_total,
      ed.demanda_orcado,
      ed.demanda_aprovado,
      ed.demanda_agendado,
      ed.demanda_utilizado,
      ed.demanda_total,
      ed.estoque_total - ed.demanda_total as necessidade_de_compra
from estoque_demanda ed
         join empresa_municipio em
              on em.id = ed.id_empresa_municipio
         join empresa e
              on e.id = em.id_empresa
         join cd_empresa_municipio cem
              on cem.id_empresa_municipio = em.id
         join centro_distribuicao cd
              on cd.id = cem.id_cd
         join produto p
              on p.viman = ed.viman
                  and ((ed.cod_empresa::int - 1) % 20) + 1 = any(string_to_array(p.cod_empresa, ',')::int[])
                  and p.cod_produto = ed.cod_produto;