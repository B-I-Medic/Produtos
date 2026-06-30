package com.medic.Web.service.auth;

import com.medic.Web.dto.auth.LoginRequestDTO;
import com.medic.Web.dto.auth.LoginResponseDTO;
import com.medic.Web.dto.auth.PasswordRequestDTO;
import com.medic.Web.dto.auth.ResetPasswordRequestDTO;
import com.medic.Web.exception.type.NotFounException;
import com.medic.Web.exception.type.auth.PasswordAlreadySetException;
import com.medic.Web.exception.type.auth.PasswordResetCodeException;
import com.medic.Web.mapper.auth.PasswordResetCodeMapper;
import com.medic.Web.model.auth.PasswordResetCodeModel;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.auth.PasswordResetCodeRepository;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.mail.MailService;
import com.medic.Web.service.mail.MailTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository repository;

    private final MailService mailService;
    private final MailTemplateService mailTemplateService;

    private final PasswordResetCodeGenerator passwordResetCodeGenerator;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordResetCodeMapper passwordResetCodeMapper;

    public AuthService(ReactiveAuthenticationManager authenticationManager,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       UsuarioRepository repository,
                       MailService mailService,
                       MailTemplateService mailTemplateService,
                       PasswordResetCodeGenerator passwordResetCodeGenerator,
                       PasswordResetCodeRepository passwordResetCodeRepository,
                       PasswordResetCodeMapper passwordResetCodeMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.mailService = mailService;
        this.mailTemplateService = mailTemplateService;
        this.passwordResetCodeGenerator = passwordResetCodeGenerator;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.passwordResetCodeMapper = passwordResetCodeMapper;
    }

    public Mono<LoginResponseDTO> login(LoginRequestDTO dto) {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.senha()
        );

        return authenticationManager.authenticate(auth)
                .map(a -> (UsuarioModel) Objects.requireNonNull(a.getPrincipal()))
                .map(this::toDTO);
    }

    @Transactional
    public Mono<Void> firstAcess(PasswordRequestDTO pss, UUID userId) {

        return repository.findById(userId)
                .filter(UsuarioModel::getAtivo)
                .switchIfEmpty(Mono.error(new DisabledException("Usuario inativo")))
                .filter(UsuarioModel::getPrimeiroAcesso)
                .switchIfEmpty(Mono.error(new PasswordAlreadySetException()))
                .map(user -> {

                    user.setSenha(passwordEncoder.encode(pss.password()));
                    user.setPrimeiroAcesso(false);
                    return user;
                })
                .flatMap(repository::save)
                .then();
    }

    private LoginResponseDTO toDTO(UsuarioModel user) {

        String token = jwtService.generateToken(
                user
        );

        return new LoginResponseDTO(
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                user.getPrimeiroAcesso(),
                token,
                jwtService.getExpires_in(token)
        );
    }

    public Mono<Void> forgotPassword(String mail) {

        String code = passwordResetCodeGenerator.generate();

        return repository.findByEmail(mail)
                .switchIfEmpty(Mono.error(new NotFounException("Usuario", mail, "email")))
                .filter(UsuarioModel::getAtivo)
                .switchIfEmpty(Mono.error(new DisabledException("Usuário inativo")))
                .flatMap(user -> {

                    PasswordResetCodeModel pssCodeEntity = passwordResetCodeMapper.toEntity(
                            mail,
                            passwordResetCodeGenerator.hash(code),
                            Instant.now().plus(10, ChronoUnit.MINUTES)
                    );

                    return passwordResetCodeRepository.save(pssCodeEntity)
                            .flatMap(codeEntity -> mailTemplateService
                                    .forgotPassword(user.getNome(), code)
                                    .flatMap(body -> mailService.sendSimpleEmail(mail, "Alteração de senha", body))
                            );
                });
    }

    @Transactional
    public Mono<Void> resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {

        return passwordResetCodeRepository.findFirstByEmailAndUsadoFalseAndExpiraEmAfterOrderByExpiraEmDesc(
                        resetPasswordRequestDTO.email(),
                        Instant.now()
                )
                .switchIfEmpty(Mono.error(new PasswordResetCodeException("Código expirado ou usado")))
                .filter(code -> passwordResetCodeGenerator.valide(resetPasswordRequestDTO.code(), code.getCodigo()))
                .switchIfEmpty(Mono.error(new PasswordResetCodeException("Código inválido")))
                .flatMap(code -> {

                    code.marcarComoUsado();

                    return passwordResetCodeRepository.save(code);
                })
                .flatMap(code -> repository.findByEmail(resetPasswordRequestDTO.email()))
                .flatMap(user -> {
                    user.setSenha(passwordEncoder.encode(resetPasswordRequestDTO.senha()));
                    return repository.save(user);
                })
                .then();
    }
}
