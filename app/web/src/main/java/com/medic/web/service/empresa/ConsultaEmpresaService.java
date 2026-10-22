package com.medic.web.service.empresa;

import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultaEmpresaService {

    private final EmpresaRepository repository;

    public ConsultaEmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginaResponseDTO<EmpresaResponseDTO>> getPage(EmpresaPaginaDTO filtro) {

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
