package com.medic.Produtos.service.usuario;

import com.medic.Produtos.dto.usuario.UsuarioRequestDTO;
import com.medic.Produtos.dto.usuario.UsuarioResponseDTO;
import com.medic.Produtos.mapper.usuario.UsuarioMapper;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.repository.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoUsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    public ManutencaoUsuarioService(UsuarioRepository repository,
                                    UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Mono<UsuarioResponseDTO> save(UsuarioRequestDTO usuario,
                                         UUID userId) {

        return Mono.just(new UsuarioModel())
                .map(user -> mapper.toEntity(user, usuario, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    public Mono<UsuarioResponseDTO> update(UUID userId,
                                           UsuarioRequestDTO usuario,
                                           UUID id) {
        
        return repository.findById(userId)
                .map(user -> mapper.toEntity(user, usuario, id))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    public Mono<UsuarioResponseDTO> disable(UUID userId,
                                            UUID id) {
        
        return repository.findById(userId)
                .map(user -> {

                    user.setAtivo(false);
                    user.setAtualizadoPor(id);

                    return user;
                })
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    public Mono<UsuarioResponseDTO> enable(UUID userId,
                                           UUID id) {

        return repository.findById(userId)
                .map(user -> {

                    user.setAtivo(true);
                    user.setAtualizadoPor(id);

                    return user;
                })
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

}
