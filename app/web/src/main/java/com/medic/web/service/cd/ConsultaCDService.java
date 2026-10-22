package com.medic.web.service.cd;

import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.cd.CentroDistribuicaoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultaCDService {

    private final CentroDistribuicaoRepository repository;

    public ConsultaCDService(CentroDistribuicaoRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginaResponseDTO<CentroDistribuicaoResponseDTO>> getPage(CentroDistribuicaoPaginaDTO filtro) {
        int pagina = Integer.parseInt(filtro.getNumeroPagina());
        int tamanhoPagina = Integer.parseInt(filtro.getTamanhoPagina());
        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        return Mono.zip(
                        repository.findPageByFilter(filtro, tamanhoPagina, deslocamento).collectList(),
                        repository.countByFilter(filtro)
                )
                .map(resultado -> {
                    int quantidadeRegistros = Math.toIntExact(resultado.getT2());
                    int quantidadePaginas = (int) Math.ceil((double) quantidadeRegistros / tamanhoPagina);
                    return new PaginaResponseDTO<>(resultado.getT1(), quantidadeRegistros, quantidadePaginas);
                });
    }
}
