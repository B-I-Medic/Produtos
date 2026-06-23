package com.medic.Produtos.dto.pagina;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PaginaResponseDTO<T>(

        List<T> pagina,

        @Schema(example = "10")
        int quantidadeRegistros,

        @Schema(example = "2")
        int quantidadePaginas
) {
}
