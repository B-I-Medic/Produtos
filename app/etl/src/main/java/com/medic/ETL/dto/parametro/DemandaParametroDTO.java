package com.medic.ETL.dto.parametro;

import lombok.Data;

@Data
public class DemandaParametroDTO {

     private PeriodoDTO orcamento;
     private PeriodoDTO aprovado;
     private PeriodoDTO agendamento;
     private PeriodoDTO cirurgia;
}
