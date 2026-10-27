package com.medic.web.dto.forecast;

import com.medic.web.dto.pagina.PaginaRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForecastFilterDTO extends PaginaRequestDTO {

    private String centroDistribuicao;
    private String empresa;
    private String estado;
    private String municipio;
    private String anvisa;
    private String marca;
    private String produto;
    private List<AgrupamentosPadrao> groupBy;
}
