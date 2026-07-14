package com.medic.ETL.service.demanda;

import com.medic.ETL.dto.parametro.DemandaParametroDTO;
import com.medic.ETL.dto.parametro.PeriodoDTO;
import com.medic.ETL.model.empresa.EmpresaModel;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.empresa.EmpresaRepository;
import com.medic.ETL.repository.periodo.PeriodoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrepararConsultaDemandaService {

    private final PeriodoRepository periodoRepository;
    private final EmpresaRepository empresaRepository;

    public PrepararConsultaDemandaService(PeriodoRepository periodoRepository,
                                          EmpresaRepository empresaRepository) {
        this.periodoRepository = periodoRepository;
        this.empresaRepository = empresaRepository;
    }

    protected String montarConsulta(Processamento processamento) {

        DemandaParametroDTO periodos = new DemandaParametroDTO();

        periodoRepository.findAll()
                .forEach(periodo -> {

                    switch (periodo.getDescricao()) {

                        case "ORCAMENTO": periodos.setOrcamento(new PeriodoDTO(periodo.getDataInicialViman(), periodo.getDataFinalViman()));
                        case "ORCAMENTO_APROVADO": periodos.setAprovado(new PeriodoDTO(periodo.getDataInicialViman(), periodo.getDataFinalViman()));
                        case "AGENDAMENTO": periodos.setAgendamento(new PeriodoDTO(periodo.getDataInicialViman(), periodo.getDataFinalViman()));
                        default:
                            periodos.setCirurgia(new PeriodoDTO(periodo.getDataInicialViman(), periodo.getDataFinalViman()));
                    }
                });

        String processamentoId = escapeSql(processamento.getId().toString());

        return montarConsultaUfx(periodos, processamentoId);
    }

    private String montarConsultaUfx(DemandaParametroDTO parametros,
                                     String processamentoId) {

        List<String> consultaList = new ArrayList<>();

        for (EmpresaModel empresa : empresaRepository.findAllByVimanAndPossuiEstoqueInternoIsTrue("UFX")) {

            String consulta = """
                    select
                        Processamento,
                        CodEmpresa,
                        IBGE,
                        CodProduto,
                        sum(base.QntOrcado) as QntOrcado,
                        sum(base.QntAprovado) as QntAprovado,
                        sum(base.QntAgendado) as QntAgendado,
                        sum(base.QntUtilizado) as QntUtilizado,
                        sum(base.QntOrcado) + sum(base.QntAprovado) + sum(base.QntAgendado) + sum(base.QntUtilizado) as QntTotal
                    from (
                        select
                            '%s' as Processamento,
                            '%s' as CodEmpresa,
                            cast(cl.CLCMFI as varchar(7)) as IBGE,
                            trim(iv.ivcodp) AS CodProduto,
                            sum(case
                                    when (pv.PVDTCD between %s and %s and pv.PVORVA = 1 and pv.PVTPVD in (17, 119)) then IV.IVQTDE
                                    else 0
                                end) as QntOrcado,
                            sum(case
                                    when ((pv.PVORDT between %s and %s and pv.PVORVA = 2) and (pv.PVTPVD in (17, 119) and pv.pvdtci = 0)) then IV.IVQTDE
                                    else 0
                                end) as QntAprovado,
                            sum(case
                                    when pv.pvdtci between %s and %s then IV.IVQTDE
                                    else 0
                                end) as QntAgendado,
                            0 as QntUtilizado
                        from sysadm.veteiv%s as IV
                              join sysadm.vetepv%s as pv
                                  on pv.pvnrpd = iv.ivnrpd
                              join sysadm.vetecl%s as cl
                                  on cl.clcodi = pv.PVCDHO and cl.clramo = 1
                        where (
                              (pv.PVDTCD between %s and %s and pv.PVORVA = 1 and pv.PVTPVD in (17, 119))
                                  or
                              ((pv.PVORDT between %s and %s and pv.PVORVA = 2) and (pv.PVTPVD in (17, 119) and pv.pvdtci = 0))
                                  or
                              (pv.pvdtci between %s and %s)
                              )
                        group by IV.IVCODP, cl.CLCMFI
                
                        union all
                    
                        select
                            '%s' as Processamento,
                            '%s' as CodEmpresa,
                            cast(cl.CLCMFI as varchar(7)) as IBGE,
                            trim(vp.vpcodp) as CodProduto,
                            0 as QntOrcado,
                            0 as QntAprovado,
                            0 as QntAgendado,
                            sum(vp.vpqtut) as QntUtilizado
                        from sysadm.vetevp%s vp
                              join sysadm.veteva%s va
                                  on va.vanume = vp.vpnume
                              join sysadm.vetecl%s cl
                                  on cl.clcodi = va.vacodc and cl.clramo = 1
                        where va.vadtpv between %s and %s
                            and va.vasitu in (50, 60, 70, 71)
                        group by vp.vpcodp, cl.CLCMFI) as base
                    group by Processamento, CodEmpresa, CodProduto, IBGE
                    """
                    .formatted(
                            processamentoId,
                            escapeSql(empresa.getCodigoEmpresa()),
                            parametros.getOrcamento().dataInicio(),
                            parametros.getOrcamento().dataFim(),
                            parametros.getAprovado().dataInicio(),
                            parametros.getAprovado().dataFim(),
                            parametros.getAgendamento().dataInicio(),
                            parametros.getAgendamento().dataFim(),
                            empresa.getCodigoEmpresa(),
                            empresa.getCodigoEmpresa(),
                            obterTabelaCliente(empresa.getCodigoEmpresa()),
                            parametros.getOrcamento().dataInicio(),
                            parametros.getOrcamento().dataFim(),
                            parametros.getAprovado().dataInicio(),
                            parametros.getAprovado().dataFim(),
                            parametros.getAgendamento().dataInicio(),
                            parametros.getAgendamento().dataFim(),
                            processamentoId,
                            escapeSql(empresa.getCodigoEmpresa()),
                            empresa.getCodigoEmpresa(),
                            empresa.getCodigoEmpresa(),
                            obterTabelaCliente(empresa.getCodigoEmpresa()),
                            parametros.getCirurgia().dataInicio(),
                            parametros.getCirurgia().dataFim()
                    );

            consultaList.add(consulta);
        }

        return String.join("\n\nUNION ALL\n\n", consultaList);
    }

    private String obterTabelaCliente(String codigoEmpresa) {

        return switch (codigoEmpresa) {
            case "07" -> "07";
            case "08" -> "08";
            case "11" -> "10";
            default -> "01";
        };
    }

    private String escapeSql(String value) {

        return value.replace("'", "''");
    }
}
