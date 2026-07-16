package com.medic.Web.dto.forecast;

import lombok.Getter;

@Getter
public enum AgrupamentosPadrao {

    CENTRO_DISTRIBUICAO("centro_distribuicao"),
    EMPRESA("empresa"),
    ESTADO("estado"),
    MUNICIPIO("municipio"),
    ANVISA("anvisa"),
    MARCA("marca"),
    COD_PRODUTO("cod_produto"),
    PRODUTO("produto");

    private final String descricao;

    AgrupamentosPadrao(String descricao) {
        this.descricao = descricao;
    }
}
