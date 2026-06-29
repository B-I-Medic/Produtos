package com.medic.Web.dto.pagina;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginaRequestDTO {

    @Schema(example = "1")
    @NotBlank(message = "O numero da pagina é obrigatório")
    private String numeroPagina;

    @Schema(example = "10")
    @NotBlank(message = "O tamanho da pagina é obrigatório")
    private String tamanhoPagina;
}
