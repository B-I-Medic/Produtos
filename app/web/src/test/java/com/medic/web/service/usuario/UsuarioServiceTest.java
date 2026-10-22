package com.medic.web.service.usuario;

import com.medic.web.config.properties.ApplicationProperties;
import com.medic.web.dto.usuario.UsuarioPaginaDTO;
import com.medic.web.dto.usuario.UsuarioRequestDTO;
import com.medic.web.dto.usuario.UsuarioResponseDTO;
import com.medic.web.mapper.usuario.UsuarioMapper;
import com.medic.web.model.usuario.Role;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.repository.usuario.UsuarioRepository;
import com.medic.web.service.mail.MailService;
import com.medic.web.service.mail.MailTemplateService;
import com.medic.web.support.TestDataFactory;
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
    @Mock
    private ApplicationProperties applicationProperties;

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
        when(applicationProperties.getLink()).thenReturn("https://produtos.surgilog.com.br/homolog/");
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
        dto.setNumeroPagina("2");
        dto.setTamanhoPagina("10");
        dto.setNome("Nome");
        dto.setOrdenarPor("nome");
        dto.setDirecaoOrdenacao("DESC");

        when(repository.findPageByFilter(dto, 10, 10)).thenReturn(Flux.just(user));
        when(repository.countByFilter(dto)).thenReturn(Mono.just(21L));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(consultaService.getPage(dto))
                .expectNextMatches(page -> page.pagina().equals(List.of(response))
                        && page.quantidadeRegistros() == 21
                        && page.quantidadePaginas() == 3)
                .verifyComplete();

        verify(repository).findPageByFilter(dto, 10, 10);
        verify(repository).countByFilter(dto);
    }

    @Test
    void shouldKeepFilteredCountWhenRequestedPageIsEmpty() {
        UsuarioPaginaDTO filter = new UsuarioPaginaDTO();
        filter.setNumeroPagina("3");
        filter.setTamanhoPagina("2");
        filter.setAtivo(false);
        when(repository.findPageByFilter(filter, 2, 4)).thenReturn(Flux.empty());
        when(repository.countByFilter(filter)).thenReturn(Mono.just(5L));

        StepVerifier.create(consultaService.getPage(filter))
                .expectNextMatches(page -> page.pagina().isEmpty()
                        && page.quantidadeRegistros() == 5
                        && page.quantidadePaginas() == 3)
                .verifyComplete();

        verify(repository).findPageByFilter(filter, 2, 4);
        verify(repository).countByFilter(filter);
    }
}
