package com.medic.Web.mapper.config.periodo;

import com.medic.Web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.service.config.periodo.PeriodoIntervalo;
import com.medic.Web.service.config.periodo.PeriodoIntervaloResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PeriodoMapper {

    private final PeriodoIntervaloResolver intervaloResolver;

    public PeriodoMapper(PeriodoIntervaloResolver intervaloResolver) {
        this.intervaloResolver = intervaloResolver;
    }

    public PeriodoModel map(PeriodoModel entity,
                            PeriodoRequestDTO periodoRequestDTO,
                            UUID userId) {

        intervaloResolver.validar(entity.getDescricao(), periodoRequestDTO.tipo(), periodoRequestDTO.quantidade());

        entity.setTipoPeriodo(periodoRequestDTO.tipo());
        entity.setQuantidade(periodoRequestDTO.quantidade());
        entity.setDataInicial(null);
        entity.setDataFinal(null);
        entity.setDataInicialViman(null);
        entity.setDataFinalViman(null);
        entity.setAtualizadoPor(userId);

        return entity;
    }

    public PeriodoResponseDTO toDTO(PeriodoModel periodoModel) {

        PeriodoIntervalo intervalo = intervaloResolver.resolver(periodoModel);

        return new PeriodoResponseDTO(
                periodoModel.getId(),
                periodoModel.getDescricao(),
                periodoModel.getTipoPeriodo(),
                periodoModel.getQuantidade(),
                intervalo.dataInicial(),
                intervalo.dataFinal()
        );
    }
}
