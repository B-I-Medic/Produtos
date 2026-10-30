package com.medic.web.dto.forecast;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForecastFilterDTO extends PaginaRequestDTO {

    private String centroDistribuicao;
    private String empresa;
    private String estado;
    private String municipio;
    private String anvisa;
    private String marca;
    private String produto;
    private List<AgrupamentosPadrao> groupBy;

    @Schema(example = "necessidadeDeCompra", allowableValues = {"centroDistribuicao", "empresa", "estado", "municipio", "anvisa", "marca", "codigoProduto", "produto", "estoqueInterno", "estoqueSegregado", "estoqueVP", "estoqueTotal", "demandaOrcado", "demandaAprovado", "demandaAgendado", "demandaUtilizado", "demandaTotal", "necessidadeDeCompra"})
    @Pattern(regexp = "(?i)^(centroDistribuicao|empresa|estado|municipio|anvisa|marca|codigoProduto|produto|estoqueInterno|estoqueSegregado|estoqueVP|estoqueTotal|demandaOrcado|demandaAprovado|demandaAgendado|demandaUtilizado|demandaTotal|necessidadeDeCompra)$", message = "Campo inválido para ordenação do forecast")
    private String ordenarPor;

    @Schema(example = "DESC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
