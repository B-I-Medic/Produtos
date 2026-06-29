package com.medic.Web.mapper.parametro.estoque;

import com.medic.Web.dto.parametro.estoque.ValePermanenteRequestDTO;
import com.medic.Web.dto.parametro.estoque.ValePermanenteResponseDTO;
import com.medic.Web.model.parametro.estoque.ValePermanenteParametroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ValePermanenteMapper {

    public ValePermanenteParametroModel toEntity(ValePermanenteParametroModel entity,
                                                 ValePermanenteRequestDTO dto,
                                                 UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setCodVp(dto.codVp());
        entity.setComporSubCd(dto.comporSubCd());

        if (entity.getId() == null) {

            entity.setCriadoPor(userId);

        } else {

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }

    public ValePermanenteResponseDTO toDTO(ValePermanenteParametroModel entity) {

        return new ValePermanenteResponseDTO(
                entity.getId(),
                entity.getIdEmpresa(),
                entity.getCodVp(),
                entity.getComporSubCd()
        );
    }
}
