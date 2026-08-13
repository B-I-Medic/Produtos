package com.medic.Web.controller.auth;

import com.medic.Web.dto.auth.LoginRequestDTO;
import com.medic.Web.dto.auth.LoginResponseDTO;
import com.medic.Web.dto.auth.PasswordRequestDTO;
import com.medic.Web.dto.auth.ResetPasswordRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Mono<LoginResponseDTO> login(@RequestBody @Valid Mono<LoginRequestDTO> dto) {

        return dto
                .flatMap(service::login);
    }

    @PostMapping("/forgot-password")
    public Mono<Void> forgotPassword(@RequestParam @Email(message = "O email está inválido") @NotBlank(message = "O email é obrigatório") String mail) {

        return service.forgotPassword(mail);
    }

    @PostMapping("/reset-password")
    public Mono<Void> resetPassword(@RequestBody @Valid Mono<ResetPasswordRequestDTO> dto) {

        return dto
                .flatMap(service::resetPassword);
    }

    @PostMapping("/first-acess")
    public Mono<Void> firstAccess(@RequestBody @Valid Mono<PasswordRequestDTO> dto,
                                 @AuthenticationPrincipal UsuarioModel user) {

        return dto
                .flatMap(pss -> service.firstAccess(pss, user.getId()));
    }
}
