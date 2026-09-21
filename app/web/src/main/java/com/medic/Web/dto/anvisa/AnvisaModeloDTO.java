package com.medic.Web.dto.anvisa;

public record AnvisaModeloDTO(
        String numeroRegistroCadastro,
        String nomeTecnico,
        String classeRisco,
        String nomeComercial,
        String detentorRegistroCadastro,
        String nomeFabricante,
        String nomePaisFabric,
        String dsModeloProdutoMedico,
        String dtPubRegistroCadastro,
        String validadeRegistroCadastro
) {
}
