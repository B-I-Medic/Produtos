package com.medic.Web.mapper.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.model.cd.CentroDistribuicaoModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CentroDistribuicaoMapper {

    public CentroDistribuicaoModel toEntity(CentroDistribuicaoModel entity,
                                            CentroDistribuicaoRequestDTO dto,
                                            UUID userId) {

        entity.setDescricao(dto.descricao());

        if (entity.getId() == null) {
//            Criando CD

            entity.setCriadoPor(userId);

        } else  {
//            Atualizando CD

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }

    public CentroDistribuicaoResponseDTO toDTO(CentroDistribuicaoModel entity) {

        return new CentroDistribuicaoResponseDTO(
                entity.getId(),
                entity.getDescricao()
        );
    }
}
