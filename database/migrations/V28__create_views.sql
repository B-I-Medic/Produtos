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
         select
             ei.viman,
             ei.cod_empresa,
             ei.cod_ibge,
             ei.cod_produto,
             ei.qnt_disponivel
         from estoque_interno ei
         where ei.processamento = (select id from ultimo_processamento_estoque)
     ),
     estoque_segregado_tratado as (
         select
             es.viman,
             es.cod_empresa,
             es.cod_ibge,
             es.cod_produto,
             es.qnt_disponivel
         from estoque_segregado es
         where es.processamento = (select id from ultimo_processamento_estoque)
     ),
     estoque_vp_tratado as (
         select
             evp.viman,
             evp.cod_empresa,
             evp.cod_ibge,
             evp.cod_produto,
             evp.qnt_disponivel
         from vale_permanente evp
         where evp.processamento = (select id from ultimo_processamento_estoque)
     ),
     estoque_final as (
         select
             coalesce(eit.viman, est.viman, evpt.viman) as viman,
             coalesce(eit.cod_empresa, est.cod_empresa, evpt.cod_empresa) as cod_empresa,
             coalesce(eit.cod_ibge, est.cod_ibge, evpt.cod_ibge) as cod_ibge,
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
    ef.viman,
    ef.cod_empresa,
    ef.cod_ibge,
    ef.cod_produto,
    sum(ef.estoque_interno) as EstoqueInterno,
    sum(ef.estoque_segregado) as EstoqueSegregado,
    sum(ef.estoque_vp) as EstoqueVP,
    sum(ef.estoque_interno) + sum(ef.estoque_segregado) + sum(ef.estoque_vp) as EstoqueTotal
from estoque_final ef
group by viman, cod_empresa, cod_ibge, cod_produto;

create view necessidade_de_compra as
with ultimo_processamento_demanda as (
    select p.id
    from processamento p
    where p.status = 'CONCLUIDO'
      and p.entidade = 'DEMANDA'
    order by p.concluido_em desc
    limit 1
),
     demanda_tratada as (
         select
             'UFX' as viman,
             d.cod_empresa as cod_empresa,
             d.cod_ibge as cod_ibge,
             d.cod_produto as cod_produto,
             qnt_orcado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO') as qnt_orcado_taxa,
             qnt_aprovado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO_APROVADO') as qnt_aprovado_taxa,
             qnt_agendado * (select t.taxa from taxa t where t.descricao = 'AGENDAMENTO') as qnt_agendado_taxa,
             qnt_utilizado * (select t.taxa from taxa t where t.descricao = 'CIRURGIA') as qnt_utilizado_taxa,
             (qnt_orcado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO') +
              qnt_aprovado * (select t.taxa from taxa t where t.descricao = 'ORCAMENTO_APROVADO')) +
             (qnt_agendado * (select t.taxa from taxa t where t.descricao = 'AGENDAMENTO') +
              qnt_utilizado * (select t.taxa from taxa t where t.descricao = 'CIRURGIA')) as demanda_total
         from demanda d
         where d.processamento = (select * from ultimo_processamento_demanda)
     ),
     estoque_demanda as (
         select
             coalesce(mv.viman, dt.viman) as viman,
             coalesce(mv.cod_empresa, dt.cod_empresa) as cod_empresa,
             coalesce(mv.cod_ibge, dt.cod_ibge) as cod_ibge,
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
                                  on mv.viman = dt.viman
                                      and mv.cod_empresa = dt.cod_empresa
                                      and mv.cod_ibge = dt.cod_ibge
                                      and mv.cod_produto = dt.cod_produto
         where mv.estoquetotal > 0 or dt.demanda_total > 0
     )
select
    ed.viman as viman,
    ed.cod_empresa as cod_empresa,
    e.descricao as empresa,
    m.descricao as municipio,
    m.estado as estado,
    cd.descricao as centro_distribuicao,
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
         join empresa e
              on e.codigo_empresa = ed.cod_empresa
                     and e.viman = ed.viman
         join municipio m
              on m.cod_ibge = ed.cod_ibge
         join centro_distribuicao cd
              on m.id_centro_distribuicao = cd.id
         join produto p
              on p.viman = ed.viman
                  and ((ed.cod_empresa::int - 1) % 20) + 1 = any(string_to_array(p.cod_empresa, ',')::int[])
                  and p.cod_produto = ed.cod_produto;
