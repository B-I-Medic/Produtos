package com.medic.Web.dto.anvisa;

public record AnvisaProdutoDTO(
        String numeroRegistroCadastro,
        String numeroProcesso,
        String nomeTecnico,
        String classeRisco,
        String nomeComercial,
        String cnpjDetentorRegistroCadastro,
        String detentorRegistroCadastro,
        String nomeFabricante,
        String nomePaisFabric,
        String dtPubRegistroCadastro,
        String validadeRegistroCadastro
) {
}
