package com.medic.Web.dto.usuario;

import com.medic.Web.dto.pagina.PaginaRequestDTO;
import com.medic.Web.model.usuario.Role;
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
