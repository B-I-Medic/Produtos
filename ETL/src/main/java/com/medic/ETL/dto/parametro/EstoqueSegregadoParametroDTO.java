package com.medic.ETL.dto.parametro;

import lombok.Data;

import java.util.UUID;

@Data
public class EstoqueSegregadoParametroDTO {

    private UUID subCd;
    private String codSegregado;
    private String viman;
    private String codEmpresa;
}
