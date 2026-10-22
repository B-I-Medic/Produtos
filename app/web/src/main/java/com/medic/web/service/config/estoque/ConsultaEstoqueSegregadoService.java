package com.medic.web.service.config.estoque;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ConsultaEstoqueSegregadoService {

    private final EstoqueSegregadoRepository repository;

    public ConsultaEstoqueSegregadoService(EstoqueSegregadoRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginaResponseDTO<EstoqueSegregadoResponseDTO>> getPage(UUID empresaId,
                                                                        EstoqueSegregadoPaginaDTO filtro) {
        int pagina = Integer.parseInt(filtro.getNumeroPagina());
        int tamanhoPagina = Integer.parseInt(filtro.getTamanhoPagina());
        long deslocamento = (long) (pagina - 1) * tamanhoPagina;
        return Mono.zip(
                        repository.findPageByEmpresaAndFilter(empresaId, filtro, tamanhoPagina, deslocamento).collectList(),
                        repository.countByEmpresaAndFilter(empresaId, filtro)
                )
                .map(resultado -> {
                    int quantidadeRegistros = Math.toIntExact(resultado.getT2());
                    int quantidadePaginas = (int) Math.ceil((double) quantidadeRegistros / tamanhoPagina);
                    return new PaginaResponseDTO<>(resultado.getT1(), quantidadeRegistros, quantidadePaginas);
                });
    }
}
