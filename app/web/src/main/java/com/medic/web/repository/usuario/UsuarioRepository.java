package com.medic.web.repository.usuario;

import com.medic.web.model.usuario.UsuarioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UsuarioRepository extends ReactiveCrudRepository<UsuarioModel, UUID>, UsuarioRepositoryCustom {

    Mono<UsuarioModel> findByEmail(String email);
}
