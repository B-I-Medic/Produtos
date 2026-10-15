package com.medic.web.dto.usuario;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import com.medic.web.model.usuario.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UsuarioPaginaDTO extends PaginaRequestDTO {

    String nome;
    String email;
    Role role;
    Boolean ativo;
}
