package com.medic.ETL.service.estoque.interno;

import com.medic.ETL.dto.consulta.EstoqueInternoConsultaDTO;
import com.medic.ETL.dto.empresa.EmpresaEstoqueInternoDTO;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class PrepararConsultaEstoqueInternoService {

    private static final Set<String> CODIGOS_UFX_GENERICOS = Set.of("01", "03", "04", "05", "06", "11", "13");

    private final EmpresaRepository empresaRepository;

    public PrepararConsultaEstoqueInternoService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public EstoqueInternoConsultaDTO montarConsultas(Processamento processamento) {

        String processamentoId = escapeSql(processamento.getId().toString());

        String consultaUfx = montarConsultaUfx(empresaRepository.findAllEstoqueInternoByViman("UFX"), processamentoId);
        String consultaS00 = montarConsultaS00(empresaRepository.findAllEstoqueInternoByViman("S00"), processamentoId);

        return new EstoqueInternoConsultaDTO(consultaUfx, consultaS00);
    }

    private String montarConsultaUfx(List<EmpresaEstoqueInternoDTO> empresas,
                                     String processamentoId) {

        List<String> consultas = new ArrayList<>();

        for (EmpresaEstoqueInternoDTO empresa : empresas) {

            if (!"UFX".equalsIgnoreCase(empresa.viman())) {
                continue;
            }

            if (CODIGOS_UFX_GENERICOS.contains(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(PU.PUCDPR) AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                        AND (PU.PUQTER - PU.PUQEDE_1) > 0
                    """.formatted(
                                processamentoId,
                                empresa.codIBGE(),
                                empresa.codigoEmpresa()
                        )
                );
                continue;
            }

            if ("07".equals(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '07' as CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(PRCDPR) AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR07
                    WHERE (PRQTER - PRQEDE_1) > 0
                    """.formatted(
                                processamentoId,
                                empresa.codIBGE()
                        )
                );
                continue;
            }

            if ("08".equals(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '08' as CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(PRCDPR) AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR08
                    WHERE (PRQTER - PRQEDE_1) > 0
                    """.formatted(
                                processamentoId,
                                empresa.codIBGE()
                        )
                );
            }
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String montarConsultaS00(List<EmpresaEstoqueInternoDTO> empresas,
                                     String processamentoId) {

        List<String> consultas = new ArrayList<>();

        for (EmpresaEstoqueInternoDTO empresa : empresas) {

            if (!"S00".equalsIgnoreCase(empresa.viman())) {
                continue;
            }

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'S00' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(PU.PUCDPR) AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                        AND (PU.PUQTER - PU.PUQEDE_1) > 0
                    """.formatted(
                            processamentoId,
                            empresa.codIBGE(),
                            empresa.codigoEmpresa()
                    )
            );
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String escapeSql(String value) {
        return value.replace("'", "''");
    }
}
