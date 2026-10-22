package com.medic.web.service.usuario;

import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.dto.usuario.UsuarioPaginaDTO;
import com.medic.web.dto.usuario.UsuarioResponseDTO;
import com.medic.web.mapper.usuario.UsuarioMapper;
import com.medic.web.repository.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultaUsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    public ConsultaUsuarioService(UsuarioRepository repository,
                                  UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Mono<PaginaResponseDTO<UsuarioResponseDTO>> getPage(UsuarioPaginaDTO usuarioPaginaDTO) {

        int pagina = Integer.parseInt(usuarioPaginaDTO.getNumeroPagina());
        int tamanhoPagina = Integer.parseInt(usuarioPaginaDTO.getTamanhoPagina());
        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        return Mono.zip(
                        repository.findPageByFilter(usuarioPaginaDTO, tamanhoPagina, deslocamento)
                                .map(mapper::toDTO)
                                .collectList(),
                        repository.countByFilter(usuarioPaginaDTO)
                )
                .map(resultado -> {
                    int quantidadeRegistros = Math.toIntExact(resultado.getT2());
                    int quantidadePaginas = (int) Math.ceil((double) quantidadeRegistros / tamanhoPagina);
                    return new PaginaResponseDTO<>(resultado.getT1(), quantidadeRegistros, quantidadePaginas);
                });
    }
}
