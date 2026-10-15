package com.medic.web.dto.anvisa;

public record AnvisaProdutoLocalConsulta(
        String viman,
        String codEmpresa,
        String codProduto,
        String descricao
) {
}
