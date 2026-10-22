package com.medic.web.dto.empresa;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import com.medic.web.model.empresa.Viman;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmpresaPaginaDTO extends PaginaRequestDTO {

    private String descricao;
    private String codigo;
    private Viman viman;
    private Boolean possuiEstoqueInterno;
    private Boolean possuiEstoqueSegregado;
    private Boolean possuiVp;

    @Schema(description = "ID do município")
    private UUID municipio;

    @Schema(example = "municipio", allowableValues = {
            "descricao", "codigo", "viman", "possui_estoque_interno", "possui_estoque_segregado", "possui_vp", "municipio"
    })
    @Pattern(regexp = "(?i)^(descricao|codigo|viman|possui_estoque_interno|possui_estoque_segregado|possui_vp|municipio)$",
            message = "Campo inválido para ordenação de empresas")
    private String ordenarPor;

    @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "A direção da ordenação deve ser ASC ou DESC")
    private String direcaoOrdenacao;
}
