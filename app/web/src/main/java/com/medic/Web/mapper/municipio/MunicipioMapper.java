package com.medic.Web.mapper.municipio;

import com.medic.Web.dto.municipio.MunicipioRequestDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.model.municipio.MunicipioModel;
import org.springframework.stereotype.Component;

@Component
public class MunicipioMapper {

    public MunicipioModel toEntity(MunicipioModel entity,
                                   MunicipioRequestDTO dto) {

        entity.setDescricao(dto.descricao());
        entity.setCodIbge(dto.codigoIbge());
        entity.setEstado(dto.estado());
        return entity;
    }

    public MunicipioResponseDTO toDTO(MunicipioModel entity) {

        return new MunicipioResponseDTO(
                entity.getId(),
                entity.getDescricao(),
                entity.getCodIbge(),
                entity.getEstado()
        );
    }
}
