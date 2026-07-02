package com.medic.Web.mapper.cd;

import com.medic.Web.model.cd.CdEmpresaMunicipioModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CdEmpresaMunipioMapper {

    public CdEmpresaMunicipioModel toEntity(CdEmpresaMunicipioModel entity,
                                            UUID cdId,
                                            UUID empresaMunicipioId,
                                            UUID userId) {

        entity.setIdCd(cdId);
        entity.setIdEmpresaMunicipio(empresaMunicipioId);
        entity.setCriadoPor(userId);

        return entity;
    }
}
