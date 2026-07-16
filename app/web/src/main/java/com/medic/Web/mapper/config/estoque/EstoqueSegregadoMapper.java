package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
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
