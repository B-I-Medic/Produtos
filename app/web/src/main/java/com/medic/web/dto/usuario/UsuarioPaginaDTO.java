package com.medic.web.dto.usuario;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import com.medic.web.model.usuario.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UsuarioPaginaDTO extends PaginaRequestDTO {

    String nome;
    String email;
    Role role;
    Boolean ativo;

    @Schema(example = "nome", allowableValues = {"id", "nome", "email", "role", "ativo"})
    @Pattern(regexp = "(?i)^(id|nome|email|role|ativo)$",
            message = "Ordenação deve ser por id, nome, email, role ou ativo")
    private String ordenarPor;

    @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
