package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.ETL.dto.parametro.EstoqueSegregadoParametroDTO;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.parametro.EstoqueSegregadoParametroRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PrepararConsultaEstoqueSegregadoService {

    private final EstoqueSegregadoParametroRepository estoqueSegregadoParametroRepository;

    public PrepararConsultaEstoqueSegregadoService(EstoqueSegregadoParametroRepository estoqueSegregadoParametroRepository) {
        this.estoqueSegregadoParametroRepository = estoqueSegregadoParametroRepository;
    }

    public EstoqueSegregadoConsultaDTO montarConsultas(Processamento processamento) {

        var parametros = estoqueSegregadoParametroRepository.obterEstoqueSegregadoParametros();

        String processamentoId = escapeSql(processamento.getId().toString());

        String consultaUfx = montarConsultaUfx(parametros, processamentoId);

        return new EstoqueSegregadoConsultaDTO(consultaUfx);
    }

    private String montarConsultaUfx(List<EstoqueSegregadoParametroDTO> empresas,
                                     String processamentoId) {

        Map<ChaveConsultaSegregado, Set<String>> codigosSegregadosPorGrupo = new LinkedHashMap<>();

        for (EstoqueSegregadoParametroDTO empresa : empresas) {

            ChaveConsultaSegregado chave = new ChaveConsultaSegregado(
                    empresa.getCodEmpresa(),
                    empresa.getCodIBGE()
            );

            codigosSegregadosPorGrupo
                    .computeIfAbsent(chave, key -> new LinkedHashSet<>())
                    .add(empresa.getCodSegregado());
        }

        List<String> consultas = new ArrayList<>();

        for (Map.Entry<ChaveConsultaSegregado, Set<String>> entry : codigosSegregadosPorGrupo.entrySet()) {

            ChaveConsultaSegregado chave = entry.getKey();

            consultas.add("""
                    SELECT
                        '%s' as Processamento,
                        'UFX' as Viman,
                        '%s' as CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(ESCDPR) AS CodProduto,
                        sum(ESQTDE) AS QntDisponivel
                    FROM SYSADM.VETEES%s
                    WHERE ESCDNC IN (%s)
                    GROUP BY ESCDPR
                    """.formatted(
                    processamentoId,
                    escapeSql(chave.codEmpresa()),
                    escapeSql(chave.codIBGE()),
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

    private record ChaveConsultaSegregado(String codEmpresa, String codIBGE) {
    }
}
