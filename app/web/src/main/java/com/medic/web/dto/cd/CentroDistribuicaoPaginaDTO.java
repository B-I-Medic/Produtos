package com.medic.web.dto.cd;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CentroDistribuicaoPaginaDTO extends PaginaRequestDTO {

    private String descricao;

    @Schema(description = "ID do município")
    private UUID municipio;

    @Schema(example = "descricao", allowableValues = {"descricao", "municipio"})
    @Pattern(regexp = "(?i)^(descricao|municipio)$", message = "Campo inválido para ordenação de centros de distribuição")
    private String ordenarPor;

    @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
