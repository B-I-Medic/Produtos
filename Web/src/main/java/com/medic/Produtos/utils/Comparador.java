package com.medic.Produtos.utils;

import static com.medic.Produtos.utils.NormalizarTexto.normalizar;

public class Comparador {

    public static boolean comparar(String campo, String filtro) {

        if (filtro == null)
            return true;

        return normalizar(campo).equals(normalizar(filtro));
    }

    public static boolean comparar(Boolean campo, Boolean filtro) {

        if (filtro == null)
            return true;

        return campo == filtro;
    }
}
