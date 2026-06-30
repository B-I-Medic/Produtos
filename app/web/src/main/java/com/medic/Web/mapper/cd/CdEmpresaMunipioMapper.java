package com.medic.Web.mapper.cd;

import com.medic.Web.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Web.dto.cd.CdEmpresaMunipioResponseDTO;
import com.medic.Web.model.cd.CdEmpresaMunipioModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CdEmpresaMunipioMapper {

    public CdEmpresaMunipioModel toEntity(CdEmpresaMunipioModel entity,
                                          CdEmpresaMunipioRequestDTO dto,
                                          UUID userId) {

        entity.setIdCd(dto.idCd());
        entity.setIdEmpresaMunicipio(dto.idEmpresaMunicipio());
        entity.setCriadoPor(userId);

        return entity;
    }

    public CdEmpresaMunipioResponseDTO toDTO(CdEmpresaMunipioModel entity) {

        return new CdEmpresaMunipioResponseDTO(
                entity.getId(),
                entity.getIdCd(),
                entity.getIdEmpresaMunicipio()
        );
    }
}
