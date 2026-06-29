package com.medic.Web.repository.usuario;

import com.medic.Web.model.usuario.UsuarioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UsuarioRepository extends ReactiveCrudRepository<UsuarioModel, UUID> {

    Mono<UsuarioModel> findByEmail(String email);
}
