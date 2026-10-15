package com.medic.web.mapper.config.estoque;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.web.model.config.estoque.EstoqueSegregadoCatalogoModel;
import com.medic.web.model.municipio.MunicipioModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EstoqueSegregadoMapper {

    public EstoqueSegregadoCatalogoModel toEntity(EstoqueSegregadoCatalogoModel entity,
                                                  EstoqueSegregadoRequestDTO dto,
                                                  MunicipioModel municipio,
                                                  UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setCodSegregado(dto.codSegregado());
        entity.setCodIBGE(municipio.getCodIbge());

        if (entity.getId() != null) {
            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }
}
