package com.medic.Web.mapper.usuario;

import com.medic.Web.dto.usuario.UsuarioRequestDTO;
import com.medic.Web.dto.usuario.UsuarioResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder;

    public UsuarioMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioModel toEntity(UsuarioModel usuario,
                                 UsuarioRequestDTO dto,
                                 UUID userId) {

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setRole(dto.role());

        if (usuario.getId() == null) {
//            Criando o usuario

            usuario.setSenha(passwordEncoder.encode(System.getenv("PRODUTO_SENHA_PADRAO")));
            usuario.setAtivo(true);
            usuario.setPrimeiroAcesso(true);
            usuario.setCriadoPor(userId);

        } else {
//            Atualizando o usuario

            usuario.setAtualizadoPor(userId);
            usuario.setAtualizadoEm(Instant.now());
        }

        return usuario;
    }

    public UsuarioResponseDTO toDTO(UsuarioModel model) {

        return new UsuarioResponseDTO(
                model.getId(),
                model.getNome(),
                model.getEmail(),
                model.getRole(),
                model.getAtivo()
        );
    }
}
