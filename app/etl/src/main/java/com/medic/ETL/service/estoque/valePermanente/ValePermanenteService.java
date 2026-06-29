package com.medic.ETL.service.estoque.valePermanente;

import com.medic.ETL.dto.consulta.ValePermanenteConsultaDTO;
import com.medic.ETL.dto.parametro.ValePermanenteParametroDTO;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.parametro.ValePermanenteParametroRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ValePermanenteService {

    private final ValePermanenteParametroRepository valePermanenteParametroRepository;

    public ValePermanenteService(ValePermanenteParametroRepository valePermanenteParametroRepository) {
        this.valePermanenteParametroRepository = valePermanenteParametroRepository;
    }

    public ValePermanenteConsultaDTO montarConsultas(Processamento processamento) {

        var parametros = valePermanenteParametroRepository.obterValePermanenteParametros();
        String processamentoId = escapeSql(processamento.getId().toString());

        String consultaUfx = montarConsultaPorBanco(parametros, processamentoId, "UFX");
        String consultaS00 = montarConsultaPorBanco(parametros, processamentoId, "S00");

        return new ValePermanenteConsultaDTO(consultaUfx, consultaS00);
    }

    private String montarConsultaPorBanco(List<ValePermanenteParametroDTO> parametros,
                                          String processamentoId,
                                          String viman) {

        Map<ChaveValePermanente, Set<String>> codigosPorGrupo = new LinkedHashMap<>();

        for (ValePermanenteParametroDTO parametro : parametros) {

            if (!viman.equalsIgnoreCase(parametro.getViman())) {
                continue;
            }

            ChaveValePermanente chave = new ChaveValePermanente(
                    parametro.getSubCd(),
                    parametro.getCodEmpresa()
            );

            codigosPorGrupo
                    .computeIfAbsent(chave, key -> new LinkedHashSet<>())
                    .add(parametro.getCodVp());
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<ChaveValePermanente, Set<String>> entry : codigosPorGrupo.entrySet()) {
            ChaveValePermanente chave = entry.getKey();

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        '%s' as Viman,
                        '%s' as CodEmpresa,
                        '%s' AS IdEmpresaMunicipio,
                        VP.VPCODP AS CodProduto,
                        sum(VP.VPQTSE) AS QntDisponivel
                    FROM SYSADM.VETEVP%s AS VP
                    JOIN SYSADM.VETEVA%s AS VA ON VA.VANUME = VP.VPNUME
                    WHERE VASITU IN (22, 25)
                        AND VP.VPQTSE > 0
                        AND VA.VATIPO = 'VP'
                        AND VA.VANUME IN (%s)
                    GROUP BY VP.VPCODP
                    """.formatted(
                    processamentoId,
                    viman,
                    escapeSql(chave.codEmpresa()),
                    escapeSql(chave.subCd().toString()),
                    escapeSql(chave.codEmpresa()),
                    escapeSql(chave.codEmpresa()),
                    quotedValues(entry.getValue())
            ));
        }

        return String.join("\nUNION ALL\n", consultas);
    }

    private String quotedValues(Set<String> values) {

        return values.stream()
                .map(this::numericValue)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String numericValue(String value) {

        return escapeSql(value);
    }

    private String escapeSql(String value) {

        return value.replace("'", "''");
    }

    private record ChaveValePermanente(UUID subCd, String codEmpresa) {
    }
}
