package com.medic.ETL.dto.parametro;

import lombok.Data;

import java.util.UUID;

@Data
public class EstoqueSegregadoParametroDTO {

    private UUID idEmpresaMunicipio;
    private String codSegregado;
    private String viman;
    private String codEmpresa;
}
