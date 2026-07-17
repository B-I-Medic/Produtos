package com.medic.Web.dto.empresa;

public record EmpresaMunicipioFilterDTO(

        String empresa,
        String municipio,
        String estado,
        String centroDistribuicao
) {
}
