package com.medic.web.dto.municipio;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class MunicipioPaginaDTO extends PaginaRequestDTO {

    private String descricao;
    private String estado;

    @Schema(description = "ID do centro de distribuição")
    private UUID centroDistribuicao;

    @Schema(example = "descricao", allowableValues = {"descricao", "estado", "centroDistribuicao"})
    @Pattern(regexp = "(?i)^(descricao|estado|centroDistribuicao)$",
            message = "Campo inválido para ordenação de municípios")
    private String ordenarPor;

    @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
