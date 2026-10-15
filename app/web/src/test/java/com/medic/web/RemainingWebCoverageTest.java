package com.medic.web;

import com.medic.web.config.mail.MailProperties;
import com.medic.web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioRequestDTO;
import com.medic.web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.web.mapper.municipio.MunicipioMapper;
import com.medic.web.mapper.usuario.UsuarioMapper;
import com.medic.web.model.cd.CentroDistribuicaoModel;
import com.medic.web.model.municipio.MunicipioModel;
import com.medic.web.model.usuario.Role;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.repository.auth.AuthRepository;
import com.medic.web.repository.usuario.UsuarioRepository;
import com.medic.web.service.mail.MailService;
import com.medic.web.utils.Comparador;
import com.medic.web.utils.NormalizarTexto;
import com.medic.web.utils.Paginacao;
import com.medic.web.exception.handler.ErrorResponseWriter;
import com.medic.web.exception.handler.JwtEntryPoint;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemainingWebCoverageTest {

    @Test
    void shouldMapUserForCreateUpdateAndResponse() {

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(nullable(String.class))).thenReturn("hash");
        UsuarioMapper mapper = new UsuarioMapper(passwordEncoder);
        UUID userId = UUID.randomUUID();

        UsuarioModel created = mapper.toEntity(new UsuarioModel(),
                new com.medic.web.dto.usuario.UsuarioRequestDTO("user@example.com", "User", Role.ADMIN), userId);
        assertEquals("hash", created.getSenha());
        assertTrue(created.getAtivo());
        assertTrue(created.getPrimeiroAcesso());
        assertEquals(userId, created.getCriadoPor());

        UsuarioModel existing = new UsuarioModel();
        existing.setId(UUID.randomUUID());
        mapper.toEntity(existing,
                new com.medic.web.dto.usuario.UsuarioRequestDTO("updated@example.com", "Updated", Role.USER), userId);
        assertEquals(userId, existing.getAtualizadoPor());
        assertNotNull(existing.getAtualizadoEm());

        existing.setAtivo(true);
        var response = mapper.toDTO(existing);
        assertEquals(existing.getId(), response.id());
        assertEquals("Updated", response.nome());
        assertEquals(Role.USER, response.role());
    }

    @Test
    void shouldMapDistributionCenterForCreateUpdateAndResponse() {

        CentroDistribuicaoMapper mapper = new CentroDistribuicaoMapper();
        UUID userId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();

        CentroDistribuicaoModel created = mapper.toEntity(new CentroDistribuicaoModel(),
                new CentroDistribuicaoRequestDTO("CD 1", municipioId), userId);
        assertEquals("CD 1", created.getDescricao());
        assertEquals(municipioId, created.getMunicipioId());
        assertEquals(userId, created.getCriadoPor());

        CentroDistribuicaoModel existing = new CentroDistribuicaoModel();
        existing.setId(UUID.randomUUID());
        mapper.toEntity(existing, new CentroDistribuicaoRequestDTO("CD 2", municipioId), userId);
        assertEquals(userId, existing.getAtualizadoPor());
        assertNotNull(existing.getAtualizadoEm());

        assertNull(mapper.toDTO(existing).municipio());
        MunicipioModel municipio = new MunicipioModel(municipioId, "Cidade", "123", "SP", null);
        assertEquals("Cidade", mapper.toDTO(existing, municipio).municipio().descricao());
    }

    @Test
    void shouldMapMunicipioForCreateAndResponse() {

        MunicipioMapper municipioMapper = new MunicipioMapper();
        MunicipioModel municipio = municipioMapper.toEntity(new MunicipioModel(),
                new MunicipioRequestDTO("Cidade", "123", "SP"));
        var municipioResponse = municipioMapper.toDTO(municipio);
        assertEquals("Cidade", municipioResponse.descricao());
        assertEquals("123", municipioResponse.codigoIbge());

        var centro = new CentroDistribuicaoResponseDTO(UUID.randomUUID(), "CD Central", null);
        assertEquals(centro, municipioMapper.toDTO(municipio, centro).centroDistribuicao());

    }

    @Test
    void shouldAuthenticateExistingUserAndRejectUnknownUser() {

        UsuarioRepository repository = mock(UsuarioRepository.class);
        AuthRepository authRepository = new AuthRepository(repository);
        UsuarioModel user = new UsuarioModel();
        user.setEmail("user@example.com");
        user.setSenha("hash");
        user.setRole(Role.ADMIN);
        when(repository.findByEmail("user@example.com")).thenReturn(Mono.just(user));

        StepVerifier.create(authRepository.findByUsername("user@example.com"))
                .assertNext(details -> {
                    assertEquals("user@example.com", details.getUsername());
                    assertEquals("hash", details.getPassword());
                    assertTrue(details.getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
                })
                .verifyComplete();

        when(repository.findByEmail("missing@example.com")).thenReturn(Mono.empty());
        StepVerifier.create(authRepository.findByUsername("missing@example.com"))
                .expectError(UsernameNotFoundException.class)
                .verify();
    }

    @Test
    void shouldCoverTextComparisonAndPagination() {

        assertEquals(" texto ".trim().toLowerCase(), NormalizarTexto.normalizar(" Texto "));
        assertEquals("", NormalizarTexto.normalizar(null));
        assertTrue(Comparador.comparar(" Texto ", "texto"));
        assertFalse(Comparador.comparar("texto", "outro"));
        assertTrue(Comparador.comparar("texto", (String) null));
        assertTrue(Comparador.comparar(Boolean.TRUE, Boolean.TRUE));
        assertFalse(Comparador.comparar(Boolean.TRUE, Boolean.FALSE));
        assertTrue(Comparador.comparar(Boolean.FALSE, null));

        new NormalizarTexto();
        new Comparador();
        new Paginacao();

        var page = Paginacao.paginar(List.of("a", "b", "c"), "2", "2");
        assertEquals(List.of("c"), page.pagina());
        assertEquals(3, page.quantidadeRegistros());
        assertEquals(2, page.quantidadePaginas());
        var empty = Paginacao.paginar(List.of(), "1", "10");
        assertEquals(0, empty.quantidadePaginas());
    }

    @Test
    void shouldWriteErrorResponsesIncludingNullFields() {

        MockServerWebExchange exchange = exchange("/coverage");
        ErrorResponseWriter writer = new ErrorResponseWriter();

        StepVerifier.create(writer.body(HttpStatus.BAD_REQUEST, null, null, exchange))
                .assertNext(body -> {
                    assertEquals("/coverage", body.path());
                    assertNull(body.erro());
                })
                .verifyComplete();

        StepVerifier.create(writer.write(HttpStatus.BAD_REQUEST, null, null, exchange))
                .verifyComplete();
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldUseFallbackMessageWhenAuthenticationExceptionHasNoMessage() {
        ErrorResponseWriter writer = mock(ErrorResponseWriter.class);
        MockServerWebExchange exchange = exchange("/secure");
        when(writer.write(any(HttpStatus.class), anyString(), nullable(String.class), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(new JwtEntryPoint(writer).commence(exchange, new BadCredentialsException(null)))
                .verifyComplete();

        verify(writer).write(HttpStatus.UNAUTHORIZED, "Autenticacao", "Token invalido", exchange);
    }

    @Test
    void shouldPropagateMailMessagingFailuresAsRuntimeErrors() throws Exception {

        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mock(MimeMessage.class);
        when(sender.createMimeMessage()).thenReturn(message);
        doThrow(new MessagingException("falha")).when(message).setSubject(anyString(), anyString());

        MailService service = new MailService(sender, new MailProperties("from@example.com"));

        StepVerifier.create(service.sendSimpleEmail("to@example.com", "subject", "body"))
                .expectError(RuntimeException.class)
                .verify();
    }

    private static MockServerWebExchange exchange(String path) {

        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }
}
