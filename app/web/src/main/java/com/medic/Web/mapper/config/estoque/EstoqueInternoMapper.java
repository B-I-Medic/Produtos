package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueInternoParametroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EstoqueInternoMapper {

    public EstoqueInternoParametroModel toEntity(EstoqueInternoParametroModel entity,
                                                 EstoqueInternoRequestDTO dto,
                                                 UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setIdEmpresaMunicipio(dto.id_empresa_municipio());

        if (entity.getId() == null) {

            entity.setCriadoPor(userId);

        } else {

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }
}
