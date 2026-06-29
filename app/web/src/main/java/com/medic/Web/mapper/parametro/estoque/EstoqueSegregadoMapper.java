package com.medic.Web.mapper.parametro.estoque;

import com.medic.Web.dto.parametro.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.parametro.estoque.EstoqueSegregadoResponseDTO;
import com.medic.Web.model.parametro.estoque.EstoqueSegregadoParametroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EstoqueSegregadoMapper {

    public EstoqueSegregadoParametroModel toEntity(EstoqueSegregadoParametroModel entity,
                                                   EstoqueSegregadoRequestDTO dto,
                                                   UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setCodSegregado(dto.codSegregado());
        entity.setComporSubCd(dto.comporSubCd());

        if (entity.getId() == null) {

            entity.setCriadoPor(userId);

        } else {

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }

    public EstoqueSegregadoResponseDTO toDTO(EstoqueSegregadoParametroModel entity) {

        return new EstoqueSegregadoResponseDTO(
                entity.getId(),
                entity.getIdEmpresa(),
                entity.getCodSegregado(),
                entity.getComporSubCd()
        );
    }
}
