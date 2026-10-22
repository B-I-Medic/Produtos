package com.medic.web.dto.pagina;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[1-9][0-9]*$", message = "O número da página deve ser maior que zero")
    private String numeroPagina;

    @Schema(example = "10")
    @NotBlank(message = "O tamanho da pagina é obrigatório")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "O tamanho da página deve ser maior que zero")
    private String tamanhoPagina;
}
