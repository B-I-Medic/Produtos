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
public class PrepararConsultaEstoqueInternoService {

    private static final Set<String> CODIGOS_UFX_GENERICOS = Set.of("01", "03", "04", "05", "06", "11", "13");

    private final EstoqueInternoParametroRepository estoqueInternoParametroRepository;

    public PrepararConsultaEstoqueInternoService(EstoqueInternoParametroRepository estoqueInternoParametroRepository) {
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

        Map<UUID, Set<String>> codigosGenericosPorEmpresaMunicipio = new LinkedHashMap<>();
        Set<UUID> empresaMunicipioComTabela07 = new LinkedHashSet<>();
        Set<UUID> empresaMunicipioComTabela08 = new LinkedHashSet<>();

        for (EstoqueInternoParametroDTO parameter : estoqueInternoParameters) {

            if (!"UFX".equalsIgnoreCase(parameter.getViman())) {
                continue;
            }

            String codViman = parameter.getCodEmpresa();
            UUID empresaMunicipio = parameter.getIdEmpresaMunicipio();

            if (CODIGOS_UFX_GENERICOS.contains(codViman)) {
                codigosGenericosPorEmpresaMunicipio
                        .computeIfAbsent(empresaMunicipio, key -> new LinkedHashSet<>())
                        .add(codViman);
                continue;
            }

            if ("07".equals(codViman)) {
                empresaMunicipioComTabela07.add(empresaMunicipio);
                continue;
            }

            if ("08".equals(codViman)) {
                empresaMunicipioComTabela08.add(empresaMunicipio);
            }
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<UUID, Set<String>> entry : codigosGenericosPorEmpresaMunicipio.entrySet()) {

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        TRIM(PU.PUCDPR) AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                    """.formatted(
                            processamentoId,
                            escapeSql(entry.getKey().toString()),
                            numericValues(entry.getValue())
                    )
            );
        }

        for (UUID idEmpresaMunicipio : empresaMunicipioComTabela07) {
            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '07' as CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        TRIM(PRCDPR) AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR07
                    """.formatted(
                            processamentoId,
                            escapeSql(idEmpresaMunicipio.toString())
                    )
            );
        }

        for (UUID idEmpresaMunicipio : empresaMunicipioComTabela08) {
            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '08' as CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        TRIM(PRCDPR) AS CodProduto,
                        (PRQTER - PRQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPR08
                    """.formatted(
                            processamentoId,
                            escapeSql(idEmpresaMunicipio.toString())
                    )
            );
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String montarConsultaS00(List<EstoqueInternoParametroDTO> estoqueInternoParameters,
                                     String processamentoId) {

        Map<UUID, Set<String>> codigosPorEmpresaMunicipio = new LinkedHashMap<>();

        for (EstoqueInternoParametroDTO parameter : estoqueInternoParameters) {

            if (!"S00".equalsIgnoreCase(parameter.getViman())) {
                continue;
            }

            codigosPorEmpresaMunicipio
                    .computeIfAbsent(parameter.getIdEmpresaMunicipio(), key -> new LinkedHashSet<>())
                    .add(parameter.getCodEmpresa());
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<UUID, Set<String>> entry : codigosPorEmpresaMunicipio.entrySet()) {

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'S00' as Viman,
                        CASE
                            WHEN PU.PUCDUS < 10 THEN '0' || CAST(PU.PUCDUS AS CHAR)
                            ELSE CAST(PU.PUCDUS AS CHAR(2))
                        END AS CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        TRIM(PU.PUCDPR) AS CodProduto,
                        (PU.PUQTER - PU.PUQEDE_1) AS QntDisponivel
                    FROM SYSADM.VETEPU PU
                    WHERE PU.PUCDUS IN (%s)
                    """.formatted(
                            processamentoId,
                            escapeSql(entry.getKey().toString()),
                            numericValues(entry.getValue())
                    )
            );
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String numericValues(Set<String> values) {

        return values.stream()
                .map(this::toNumericValue)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String toNumericValue(String value) {

        return Integer.toString(Integer.parseInt(value));
    }

    private String escapeSql(String value) {
        return value.replace("'", "''");
    }
}
