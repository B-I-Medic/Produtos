package com.medic.web.service.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.municipio.MunicipioRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultaMunicipioService {

    private final MunicipioRepository repository;

    public ConsultaMunicipioService(MunicipioRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginaResponseDTO<MunicipioResponseDTO>> getPage(MunicipioPaginaDTO filtro) {

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
