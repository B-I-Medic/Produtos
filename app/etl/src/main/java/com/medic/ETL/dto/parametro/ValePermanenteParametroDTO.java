package com.medic.ETL.dto.parametro;

import lombok.Data;

import java.util.UUID;

@Data
public class ValePermanenteParametroDTO {

    private UUID subCd;
    private String codVp;
    private String viman;
    private String codEmpresa;
}
