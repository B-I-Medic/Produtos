package com.medic.Produtos.utils;

import com.medic.Produtos.dto.pagina.PaginaResponseDTO;

import java.util.List;

public class Paginacao {

    public static <T> PaginaResponseDTO<T> paginar(List<T> itens, String numeroPaginaRequest, String tamanhoPaginaRequest) {

        int numeroPagina = Integer.parseInt(numeroPaginaRequest);
        int tamanhoPagina = Integer.parseInt(tamanhoPaginaRequest);

        int quantidadeRegistros = itens.size();
        int quantidadePaginas = (int) Math.ceil((double) quantidadeRegistros / tamanhoPagina);

        if (itens.isEmpty()) return new PaginaResponseDTO<>(itens, 0, 0);

        var inicio = (numeroPagina - 1) * tamanhoPagina;
        var fim = Math.min(
                inicio + tamanhoPagina,
                itens.size()
        );

        return new PaginaResponseDTO<>(itens.subList(inicio, fim), quantidadeRegistros, quantidadePaginas);
    }
}
