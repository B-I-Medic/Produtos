package com.medic.web.repository.usuario;

import com.medic.web.dto.usuario.UsuarioPaginaDTO;
import com.medic.web.model.usuario.UsuarioModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsuarioRepositoryCustom {

    Flux<UsuarioModel> findPageByFilter(UsuarioPaginaDTO filtro, long limite, long deslocamento);

    Mono<Long> countByFilter(UsuarioPaginaDTO filtro);
}
