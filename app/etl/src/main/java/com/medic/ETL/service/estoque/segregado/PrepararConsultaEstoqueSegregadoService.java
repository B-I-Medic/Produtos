package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoDTO;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.catalogo.EstoqueSegregadoCatalogoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PrepararConsultaEstoqueSegregadoService {

    private final EstoqueSegregadoCatalogoRepository estoqueSegregadoCatalogoRepository;

    public PrepararConsultaEstoqueSegregadoService(EstoqueSegregadoCatalogoRepository estoqueSegregadoCatalogoRepository) {
        this.estoqueSegregadoCatalogoRepository = estoqueSegregadoCatalogoRepository;
    }

    public EstoqueSegregadoConsultaDTO montarConsultas(Processamento processamento) {

        var empresas = estoqueSegregadoCatalogoRepository.obterEstoqueSegregadoCatalogo();
        String processamentoId = processamento.getId().toString().replace("'", "''");

        return new EstoqueSegregadoConsultaDTO(
                montarConsulta(empresas, processamentoId, "UFX"),
                montarConsulta(empresas, processamentoId, "S00")
        );
    }

    private String montarConsulta(List<EstoqueSegregadoCatalogoDTO> empresas,
                                  String processamentoId,
                                  String viman) {

        Map<ChaveConsultaSegregado, Set<String>> codigosSegregadosPorGrupo = new LinkedHashMap<>();

        for (EstoqueSegregadoCatalogoDTO empresa : empresas) {

            if (!viman.equalsIgnoreCase(empresa.getViman())) {
                continue;
            }

            String codigoEmpresa = validarCodigo(empresa.getCodEmpresa());
            String codIbge = validarCodIbge(empresa.getCodIBGE());

            ChaveConsultaSegregado chave = new ChaveConsultaSegregado(
                    codigoEmpresa,
                    codIbge
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
                        '%s' as Viman,
                        '%s' as CodEmpresa,
                        '%s' AS CodIBGE,
                        TRIM(ESCDPR) AS CodProduto,
                        sum(ESQTDE) AS QntDisponivel
                    FROM SYSADM.VETEES%s
                    WHERE ESCDNC IN (%s)
                    GROUP BY ESCDPR
                    """.formatted(
                    processamentoId,
                    viman,
                    escapeSql(chave.codEmpresa()),
                    escapeSql(chave.codIBGE()),
                    chave.codEmpresa(),
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

        try {
            return Integer.toString(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Codigo de estoque segregado invalido: " + value, exception);
        }
    }

    private String validarCodigo(String value) {

        if (value == null || !value.trim().matches("\\d{1,2}")) {
            throw new IllegalArgumentException("Codigo de empresa invalido para consulta de estoque segregado: " + value);
        }

        return value.trim();
    }

    private String validarCodIbge(String value) {

        if (value == null || !value.trim().matches("\\d{1,10}")) {
            throw new IllegalArgumentException("Codigo IBGE invalido para consulta de estoque segregado: " + value);
        }

        return value.trim();
    }

    private String escapeSql(String value) {

        return value == null ? "" : value.replace("'", "''");
    }

    private record ChaveConsultaSegregado(String codEmpresa, String codIBGE) {
    }
}
