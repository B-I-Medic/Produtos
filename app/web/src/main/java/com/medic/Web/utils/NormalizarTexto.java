package com.medic.Web.utils;

public class NormalizarTexto {

    public static String normalizar(String texto) {

        if (texto == null)
            return "";

        return texto.trim().toLowerCase();
    }
}
