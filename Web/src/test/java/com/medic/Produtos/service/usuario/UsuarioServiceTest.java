package com.medic.Produtos.service.usuario;

import com.medic.Produtos.dto.usuario.UsuarioPaginaDTO;
import com.medic.Produtos.dto.usuario.UsuarioRequestDTO;
import com.medic.Produtos.dto.usuario.UsuarioResponseDTO;
import com.medic.Produtos.mapper.usuario.UsuarioMapper;
import com.medic.Produtos.model.usuario.Role;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.repository.usuario.UsuarioRepository;
import com.medic.Produtos.support.TestDataFactory;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private UsuarioMapper mapper;

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
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.save(dto, UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
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
    void shouldDisableAndEnableUsuario() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        UsuarioResponseDTO response = TestDataFactory.usuarioResponseDTO();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(mapper.toDTO(user)).thenReturn(response);

        StepVerifier.create(manutencaoService.disable(user.getId(), UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();

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
