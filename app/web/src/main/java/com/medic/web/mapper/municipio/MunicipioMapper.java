package com.medic.web.mapper.municipio;

import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioRequestDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.model.municipio.MunicipioModel;
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

    public MunicipioResponseDTO toDTO(MunicipioModel entity,
                                      CentroDistribuicaoResponseDTO centroDistribuicao) {

        return new MunicipioResponseDTO(
                entity.getId(),
                entity.getDescricao(),
                entity.getCodIbge(),
                entity.getEstado(),
                centroDistribuicao
        );
    }
}
