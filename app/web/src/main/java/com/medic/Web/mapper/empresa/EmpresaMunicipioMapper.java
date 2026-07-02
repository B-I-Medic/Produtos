package com.medic.Web.mapper.empresa;

import com.medic.Web.model.empresa.EmpresaMunicipioModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmpresaMunicipioMapper {

    public EmpresaMunicipioModel toEntity(EmpresaMunicipioModel entity,
                                          UUID empresaId,
                                          UUID municipioId,
                                          UUID userId) {

        entity.setIdEmpresa(empresaId);
        entity.setIdMunicipio(municipioId);
        entity.setCriadoPor(userId);

        return entity;
    }
}
