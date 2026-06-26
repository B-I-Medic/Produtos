package com.medic.Produtos.dto.usuario;

import com.medic.Produtos.dto.pagina.PaginaRequestDTO;
import com.medic.Produtos.model.usuario.Role;
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
