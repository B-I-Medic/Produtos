package com.medic.ETL.service.estoque.interno;

import com.medic.ETL.dto.consulta.EstoqueInternoConsultaDTO;
import com.medic.ETL.dto.parametro.EstoqueInternoParametroDTO;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.parametro.EstoqueInternoParametroRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
public class EstoqueInternoService {

    private static final Set<String> CODIGOS_UFX_GENERICOS = Set.of("01", "03", "04", "05", "06", "11", "13");

    private final EstoqueInternoParametroRepository estoqueInternoParametroRepository;

    public EstoqueInternoService(EstoqueInternoParametroRepository estoqueInternoParametroRepository) {
        this.estoqueInternoParametroRepository = estoqueInternoParametroRepository;
    }

    public EstoqueInternoConsultaDTO montarConsultas(Processamento processamento) {

        var estoqueInternoParameters = estoqueInternoParametroRepository.obterEstoqueInternoParametros();

        String processamentoId = escapeSql(processamento.getId().toString());

        String consultaUfx = montarConsultaUfx(estoqueInternoParameters, processamentoId);
        String consultaS00 = montarConsultaS00(estoqueInternoParameters, processamentoId);

        return new EstoqueInternoConsultaDTO(consultaUfx, consultaS00);
    }

    private String montarConsultaUfx(List<EstoqueInternoParametroDTO> estoqueInternoParameters,
                                     String processamentoId) {

        Map<UUID, Set<String>> codigosGenericosPorSubCd = new LinkedHashMap<>();
        Set<UUID> subCdsComTabela07 = new LinkedHashSet<>();
        Set<UUID> subCdsComTabela08 = new LinkedHashSet<>();

        for (EstoqueInternoParametroDTO parameter : estoqueInternoParameters) {

            if (!"UFX".equalsIgnoreCase(parameter.getViman())) {
                continue;
            }

            String codViman = parameter.getCodEmpresa();
            UUID subCd = parameter.getSubCd();

            if (CODIGOS_UFX_GENERICOS.contains(codViman)) {
                codigosGenericosPorSubCd
                        .computeIfAbsent(subCd, key -> new LinkedHashSet<>())
                        .add(codViman);
                continue;
            }

            if ("07".equals(codViman)) {
                subCdsComTabela07.add(subCd);
                continue;
            }

            if ("08".equals(codViman)) {
                subCdsComTabela08.add(subCd);
            }
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<UUID, Set<String>> entry : codigosGenericosPorSubCd.entrySet()) {

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        PU.PUCDPR AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                    """.formatted(
                            processamentoId,
                            escapeSql(entry.getKey().toString()),
                            quotedValues(entry.getValue())
                    )
            );
        }

        for (UUID subCd : subCdsComTabela07) {
            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '07' as CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        PRCDPR AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR07
                    """.formatted(
                            processamentoId,
                            escapeSql(subCd.toString())
                    )
            );
        }

        for (UUID subCd : subCdsComTabela08) {
            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '08' as CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        PRCDPR AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR08
                    """.formatted(
                            processamentoId,
                            escapeSql(subCd.toString())
                    )
            );
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String montarConsultaS00(List<EstoqueInternoParametroDTO> estoqueInternoParameters,
                                     String processamentoId) {

        Map<UUID, Set<String>> codigosPorSubCd = new LinkedHashMap<>();

        for (EstoqueInternoParametroDTO parameter : estoqueInternoParameters) {

            if (!"S00".equalsIgnoreCase(parameter.getViman())) {
                continue;
            }

            codigosPorSubCd
                    .computeIfAbsent(parameter.getSubCd(), key -> new LinkedHashSet<>())
                    .add(parameter.getCodEmpresa());
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<UUID, Set<String>> entry : codigosPorSubCd.entrySet()) {

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'S00' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        PU.PUCDPR AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                    """.formatted(
                            processamentoId,
                            escapeSql(entry.getKey().toString()),
                            quotedValues(entry.getValue())
                    )
            );
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String quotedValues(Set<String> values) {

        return values.stream()
                .map(this::quote)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String quote(String value) {

        return "'" + escapeSql(value) + "'";
    }

    private String escapeSql(String value) {
        return value.replace("'", "''");
    }
}
