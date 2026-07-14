package com.medic.ETL.dto.parametro;

import lombok.Data;

import java.util.UUID;

@Data
public class EstoqueInternoParametroDTO {

    private UUID idEmpresaMunicipio;
    private String viman;
    private String codEmpresa;
}
