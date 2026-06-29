package com.medic.Web.mapper.parametro.estoque;

import com.medic.Web.dto.parametro.estoque.EstoqueInternoRequestDTO;
import com.medic.Web.dto.parametro.estoque.EstoqueInternoResponseDTO;
import com.medic.Web.model.parametro.estoque.EstoqueInternoParametroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EstoqueInternoMapper {

    public EstoqueInternoParametroModel toEntity(EstoqueInternoParametroModel entity,
                                                 EstoqueInternoRequestDTO dto,
                                                 UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setComporSubCd(dto.comporSubCd());

        if (entity.getId() == null) {

            entity.setCriadoPor(userId);

        } else {

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }

    public EstoqueInternoResponseDTO toDTO(EstoqueInternoParametroModel entity) {

        return new EstoqueInternoResponseDTO(
                entity.getId(),
                entity.getIdEmpresa(),
                entity.getComporSubCd()
        );
    }
}
