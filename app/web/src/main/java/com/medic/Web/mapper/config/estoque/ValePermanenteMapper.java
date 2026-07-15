package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.ValePermanenteRequestDTO;
import com.medic.Web.dto.config.estoque.ValePermanenteResponseDTO;
import com.medic.Web.model.config.estoque.ValePermanenteParametroModel;
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
        entity.setIdEmpresaMunicipio(dto.id_empresa_municipio());

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
                entity.getIdEmpresaMunicipio()
        );
    }
}
