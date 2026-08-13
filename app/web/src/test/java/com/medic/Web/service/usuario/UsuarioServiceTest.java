package com.medic.Web.service.usuario;

import com.medic.Web.dto.usuario.UsuarioPaginaDTO;
import com.medic.Web.dto.usuario.UsuarioRequestDTO;
import com.medic.Web.dto.usuario.UsuarioResponseDTO;
import com.medic.Web.mapper.usuario.UsuarioMapper;
import com.medic.Web.model.usuario.Role;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.mail.MailService;
import com.medic.Web.service.mail.MailTemplateService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private UsuarioMapper mapper;
    @Mock
    private MailService mailService;
    @Mock
    private MailTemplateService mailTemplateService;

    @InjectMocks
    private ManutencaoUsuarioService manutencaoService;
    @InjectMocks
    private ConsultaUsuarioService consultaService;

    @Test
    void shouldSaveUsuario() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        UsuarioRequestDTO dto = new UsuarioRequestDTO("teste@medic.com", "Teste", Role.ADMIN);
        when(mapper.toEntity(org.mockito.ArgumentMatchers.any(UsuarioModel.class), org.mockito.ArgumentMatchers.eq(dto), org.mockito.ArgumentMatchers.any(UUID.class))).thenReturn(user);
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(mailTemplateService.newUserAccess(user.getNome(), System.getenv("PRODUTO_SENHA_PADRAO"), "https://produtos.surgilog.com.br/homolog/"))
                .thenReturn(Mono.just("body"));
        when(mailService.sendSimpleEmail(user.getEmail(), "Acesso liberado", "body"))
                .thenReturn(Mono.empty());
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.save(dto, UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();

        verify(mailService).sendSimpleEmail(user.getEmail(), "Acesso liberado", "body");
    }

    @Test
    void shouldUpdateUsuario() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        UsuarioRequestDTO dto = new UsuarioRequestDTO("teste@medic.com", "Teste", Role.ADMIN);
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(mapper.toEntity(user, dto, user.getId())).thenReturn(user);
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.update(user.getId(), dto, user.getId()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldDisableUsuario() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.disable(user.getId(), UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldEnableUsuario() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.enable(user.getId(), UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldGetPage() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        UsuarioPaginaDTO dto = new UsuarioPaginaDTO();
        dto.setNumeroPagina("1");
        dto.setTamanhoPagina("10");

        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(user)));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(consultaService.getPage(dto))
                .expectNextMatches(page -> page.pagina().size() == 1 && page.quantidadeRegistros() == 1)
                .verifyComplete();
    }
}
