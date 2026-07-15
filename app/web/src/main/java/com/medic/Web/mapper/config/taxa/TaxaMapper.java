package com.medic.Web.mapper.config.taxa;

import com.medic.Web.dto.config.taxa.TaxaRequestDTO;
import com.medic.Web.dto.config.taxa.TaxaResponseDTO;
import com.medic.Web.model.config.taxa.TaxaModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TaxaMapper {

    public TaxaModel update(TaxaModel entity,
                            TaxaRequestDTO taxaResponseDTO,
                            UUID userId) {

        entity.setTaxa(taxaResponseDTO.taxa());
        entity.setAtualizadoPor(userId);

        return entity;
    }

    public TaxaResponseDTO toDTO(TaxaModel taxaModel) {

        return new TaxaResponseDTO(
                taxaModel.getId(),
                taxaModel.getDescricao(),
                taxaModel.getTaxa()
        );
    }
}
