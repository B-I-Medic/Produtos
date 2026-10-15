package com.medic.etl.service.estoque.valePermanente;

import com.medic.etl.dto.consulta.ValePermanenteConsultaDTO;
import com.medic.etl.dto.empresa.EmpresaValePermanenteDTO;
import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PrepararConsultaValePermanenteService {

    private final Set<String> CODIGOS_UFX_GENERICOS = Set.of("01", "03", "04", "05", "06", "13");

    private final EmpresaRepository empresaRepository;

    public PrepararConsultaValePermanenteService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public ValePermanenteConsultaDTO montarConsultas(Processamento processamento) {

        String processamentoId = escapeSql(processamento.getId().toString());

        String consultaUfx = montarConsultaUFX(empresaRepository.findAllValePermanenteByViman("UFX"), processamentoId);
        String consultaS00 = montarConsultaS00(empresaRepository.findAllValePermanenteByViman("S00"), processamentoId);

        return new ValePermanenteConsultaDTO(consultaUfx, consultaS00);
    }

    private String montarConsultaUFX(List<EmpresaValePermanenteDTO> empresas,
                                     String processamentoId) {

        List<String> consultas = new ArrayList<>();

        for (EmpresaValePermanenteDTO empresa : empresas) {

            if (CODIGOS_UFX_GENERICOS.contains(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        '%s' as Viman,
                        '%s' as CodEmpresa,
                        CL.CLCMFI AS CodIBGE,
                        TRIM(VP.VPCODP) AS CodProduto,
                        sum(VP.VPQTSE) AS QntDisponivel
                    FROM SYSADM.VETEVP%s AS VP
                    JOIN SYSADM.VETEVA%s AS VA ON VA.VANUME = VP.VPNUME
                    JOIN SYSADM.VETECL01 AS CL ON CL.CLCODI = VA.VACODC AND CL.CLRAMO = VA.VARACL
                    WHERE VASITU IN (22, 25)
                        AND VP.VPQTSE > 0
                        AND VA.VATIPO = 'VP'
                    GROUP BY VP.VPCODP, CL.CLCMFI
                    """.formatted(
                        processamentoId,
                        empresa.viman(),
                        escapeSql(empresa.codigoEmpresa()),
                        escapeSql(empresa.codigoEmpresa()),
                        escapeSql(empresa.codigoEmpresa())
                ));
            }

            if ("07".equalsIgnoreCase(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        '%s' as Viman,
                        '07' as CodEmpresa,
                        CL.CLCMFI AS CodIBGE,
                        TRIM(VP.VPCODP) AS CodProduto,
                        sum(VP.VPQTSE) AS QntDisponivel
                    FROM SYSADM.VETEVP07 AS VP
                    JOIN SYSADM.VETEVA07 AS VA ON VA.VANUME = VP.VPNUME
                    JOIN SYSADM.VETECL07 AS CL ON CL.CLCODI = VA.VACODC AND CL.CLRAMO = VA.VARACL
                    WHERE VASITU IN (22, 25)
                        AND VP.VPQTSE > 0
                        AND VA.VATIPO = 'VP'
                    GROUP BY VP.VPCODP, CL.CLCMFI
                    """.formatted(
                        processamentoId,
                        empresa.viman()
                ));
            }

            if ("08".equalsIgnoreCase(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        '%s' as Viman,
                        '08' as CodEmpresa,
                        CL.CLCMFI AS CodIBGE,
                        TRIM(VP.VPCODP) AS CodProduto,
                        sum(VP.VPQTSE) AS QntDisponivel
                    FROM SYSADM.VETEVP08 AS VP
                    JOIN SYSADM.VETEVA08 AS VA ON VA.VANUME = VP.VPNUME
                    JOIN SYSADM.VETECL08 AS CL ON CL.CLCODI = VA.VACODC AND CL.CLRAMO = VA.VARACL
                    WHERE VASITU IN (22, 25)
                        AND VP.VPQTSE > 0
                        AND VA.VATIPO = 'VP'
                    GROUP BY VP.VPCODP, CL.CLCMFI
                    """.formatted(
                        processamentoId,
                        empresa.viman()
                ));
            }

            if ("11".equalsIgnoreCase(empresa.codigoEmpresa())) {

                consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        '%s' as Viman,
                        '11' as CodEmpresa,
                        CL.CLCMFI AS CodIBGE,
                        TRIM(VP.VPCODP) AS CodProduto,
                        sum(VP.VPQTSE) AS QntDisponivel
                    FROM SYSADM.VETEVP11 AS VP
                    JOIN SYSADM.VETEVA11 AS VA ON VA.VANUME = VP.VPNUME
                    JOIN SYSADM.VETECL10 AS CL ON CL.CLCODI = VA.VACODC AND CL.CLRAMO = VA.VARACL
                    WHERE VASITU IN (22, 25)
                        AND VP.VPQTSE > 0
                        AND VA.VATIPO = 'VP'
                    GROUP BY VP.VPCODP, CL.CLCMFI
                    """.formatted(
                        processamentoId,
                        empresa.viman()
                ));
            }

        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String montarConsultaS00(List<EmpresaValePermanenteDTO> empresas,
                                     String processamentoId) {

        List<String> consultas = new ArrayList<>();

        for (EmpresaValePermanenteDTO empresa : empresas) {

            consultas.add("""
                SELECT
                    '%s' as Processamento,
                    '%s' as Viman,
                    '%s' as CodEmpresa,
                    CL.CLCMFI AS CodIBGE,
                    TRIM(VP.VPCODP) AS CodProduto,
                    sum(VP.VPQTSE) AS QntDisponivel
                FROM SYSADM.VETEVP%s AS VP
                JOIN SYSADM.VETEVA%s AS VA ON VA.VANUME = VP.VPNUME
                JOIN SYSADM.VETECL%s AS CL ON CL.CLCODI = VA.VACODC AND CL.CLRAMO = VA.VARACL
                WHERE VASITU IN (22, 25)
                    AND VP.VPQTSE > 0
                    AND VA.VATIPO = 'VP'
                GROUP BY VP.VPCODP, CL.CLCMFI
                """.formatted(
                    processamentoId,
                    empresa.viman(),
                    escapeSql(empresa.codigoEmpresa()),
                    escapeSql(empresa.codigoEmpresa()),
                    escapeSql(empresa.codigoEmpresa()),
                    normalizeCodEmpresa(empresa.codigoEmpresa())
            ));
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String escapeSql(String value) {

        return value.replace("'", "''");
    }

    private String normalizeCodEmpresa(String codigo) {

        int codigoRaiz = ((Integer.parseInt(codigo) - 1) % 20) + 1;

        return codigoRaiz < 10 ? "0" + codigoRaiz : Integer.toString(codigoRaiz);
    }
}
