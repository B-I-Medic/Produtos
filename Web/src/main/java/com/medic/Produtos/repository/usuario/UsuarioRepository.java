package com.medic.Produtos.repository.usuario;

import com.medic.Produtos.model.usuario.UsuarioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UsuarioRepository extends ReactiveCrudRepository<UsuarioModel, UUID> {

    Mono<UsuarioModel> findByEmail(String email);
}
