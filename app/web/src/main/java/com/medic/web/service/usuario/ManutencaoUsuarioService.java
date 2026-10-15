package com.medic.web.service.usuario;

import com.medic.web.config.properties.ApplicationProperties;
import com.medic.web.dto.usuario.UsuarioRequestDTO;
import com.medic.web.dto.usuario.UsuarioResponseDTO;
import com.medic.web.mapper.usuario.UsuarioMapper;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.repository.usuario.UsuarioRepository;
import com.medic.web.service.mail.MailService;
import com.medic.web.service.mail.MailTemplateService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoUsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final MailService mailService;
    private final MailTemplateService mailTemplateService;
    private final ApplicationProperties applicationProperties;

    public ManutencaoUsuarioService(UsuarioRepository repository,
                                    UsuarioMapper mapper,
                                    MailService mailService,
                                    MailTemplateService mailTemplateService,
                                    ApplicationProperties applicationProperties) {
        this.repository = repository;
        this.mapper = mapper;
        this.mailService = mailService;
        this.mailTemplateService = mailTemplateService;
        this.applicationProperties = applicationProperties;
    }

    public Mono<UsuarioResponseDTO> save(UsuarioRequestDTO usuario,
                                         UUID userId) {

        return Mono.just(new UsuarioModel())
                .map(user -> mapper.toEntity(user, usuario, userId))
                .flatMap(repository::save)
                .flatMap(user -> mailTemplateService
                        .newUserAccess(
                                user.getNome(),
                                System.getenv("PRODUTO_SENHA_PADRAO"),
                                applicationProperties.getLink()
                        )
                        .flatMap(body -> mailService.sendSimpleEmail(
                                user.getEmail(),
                                "Acesso liberado",
                                body
                        ))
                        .thenReturn(user)
                )
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
