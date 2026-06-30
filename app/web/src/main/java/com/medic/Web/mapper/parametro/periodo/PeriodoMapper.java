package com.medic.Web.mapper.parametro.periodo;

import com.medic.Web.dto.parametro.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.parametro.periodo.PeriodoResponseDTO;
import com.medic.Web.model.parametro.periodo.PeriodoModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.medic.Web.utils.VimanDateFormatter.formatterToViman;

@Component
public class PeriodoMapper {

    public PeriodoModel map(PeriodoModel entity,
                            PeriodoRequestDTO periodoResponseDTO,
                            UUID userId) {

        entity.setDataInicial(periodoResponseDTO.dataInicial());
        entity.setDataFinal(periodoResponseDTO.dataFinal());
        entity.setDataInicialViman(formatterToViman(periodoResponseDTO.dataInicial()));
        entity.setDataFinalViman(formatterToViman(periodoResponseDTO.dataFinal()));
        entity.setAtualizadoPor(userId);

        return entity;
    }

    public PeriodoResponseDTO toDTO(PeriodoModel periodoModel) {

        return new PeriodoResponseDTO(
                periodoModel.getId(),
                periodoModel.getDescricao(),
                periodoModel.getDataInicial(),
                periodoModel.getDataFinal()
        );
    }
}
