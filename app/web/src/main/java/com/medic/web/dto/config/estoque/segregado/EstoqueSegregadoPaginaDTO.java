package com.medic.web.dto.config.estoque.segregado;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class EstoqueSegregadoPaginaDTO extends PaginaRequestDTO {

    private String descricao;

    @Schema(description = "ID do município")
    private UUID municipio;

    @Schema(example = "municipio", allowableValues = {"descricao", "municipio"})
    @Pattern(regexp = "(?i)^(descricao|municipio)$", message = "Campo inválido para ordenação de estoques segregados")
    private String ordenarPor;

    @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
