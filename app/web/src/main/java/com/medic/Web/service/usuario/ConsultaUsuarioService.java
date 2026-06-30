package com.medic.Web.service.usuario;

import com.medic.Web.dto.pagina.PaginaResponseDTO;
import com.medic.Web.dto.usuario.UsuarioPaginaDTO;
import com.medic.Web.dto.usuario.UsuarioResponseDTO;
import com.medic.Web.mapper.usuario.UsuarioMapper;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.medic.Web.utils.Comparador.comparar;
import static com.medic.Web.utils.Paginacao.paginar;

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

        return repository.findAll()
                .filter(usuario -> filtrar(usuario, usuarioPaginaDTO))
                .map(mapper::toDTO)
                .collectList()
                .map(usuarios -> paginar(
                        usuarios,
                        usuarioPaginaDTO.getNumeroPagina(),
                        usuarioPaginaDTO.getTamanhoPagina())
                );
    }

    private boolean filtrar(UsuarioModel usuario, UsuarioPaginaDTO filtro) {

        boolean matchNome = comparar(usuario.getNome(), filtro.getNome());
        boolean matchEmail = comparar(usuario.getEmail(), filtro.getEmail());
        boolean matchAtivo = comparar(usuario.getAtivo(), filtro.getAtivo());

        String roleString = filtro.getRole() != null ? filtro.getRole().name() : null;
        boolean matchRole = comparar(usuario.getRole().name(), roleString);

        return matchNome && matchEmail && matchAtivo && matchRole;
    }
}
