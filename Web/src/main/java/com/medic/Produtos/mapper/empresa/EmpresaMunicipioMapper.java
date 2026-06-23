package com.medic.Produtos.mapper.empresa;

import com.medic.Produtos.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Produtos.model.empresa.EmpresaMunicipioModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmpresaMunicipioMapper {

    public EmpresaMunicipioModel toEntity(EmpresaMunicipioModel entity,
                                          EmpresaMunicipioRequestDTO dto,
                                          UUID userId) {

        entity.setIdEmpresa(dto.idEmpresa());
        entity.setIdMunicipio(dto.idMunicipio());
        entity.setCriadoPor(userId);

        return entity;
    }

    public EmpresaMunicipioResponseDTO toDTO(EmpresaMunicipioModel entity) {

        return new EmpresaMunicipioResponseDTO(
                entity.getId(),
                entity.getIdEmpresa(),
                entity.getIdMunicipio()
        );
    }
}
