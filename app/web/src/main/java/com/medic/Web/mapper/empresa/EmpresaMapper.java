package com.medic.Web.mapper.empresa;

import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.dto.municipio.MunicipioResumoResponseDTO;
import com.medic.Web.model.empresa.EmpresaModel;
import com.medic.Web.model.municipio.MunicipioModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EmpresaMapper {

    public EmpresaModel toEntity(EmpresaModel entity,
                                 EmpresaRequestDTO dto,
                                 UUID userId) {

        entity.setDescricao(dto.descricao());
        entity.setMunicipioId(dto.municipioId());
        entity.setViman(dto.viman());
        entity.setCodigoEmpresa(dto.codigoEmpresa());
        entity.setPossuiEstoqueInterno(dto.possuiEstoqueInterno());
        entity.setPossuiEstoqueSegregado(dto.possuiEstoqueSegregado());
        entity.setPossuiVp(dto.possuiVp());

        if (entity.getId() == null) {

            entity.setCriadoPor(userId);

        } else {

            entity.setAtualizadoPor(userId);
            entity.setAtualizadoEm(Instant.now());
        }

        return entity;
    }

    public EmpresaResponseDTO toDTO(EmpresaModel entity) {

        return toDTO(entity, null);
    }

    public EmpresaResponseDTO toDTO(EmpresaModel entity, MunicipioModel municipio) {

        return new EmpresaResponseDTO(
                entity.getId(),
                entity.getDescricao(),
                municipio == null ? null : new MunicipioResumoResponseDTO(
                        municipio.getId(),
                        municipio.getDescricao(),
                        municipio.getEstado()
                ),
                entity.getViman(),
                entity.getCodigoEmpresa(),
                entity.isPossuiEstoqueInterno(),
                entity.isPossuiEstoqueSegregado(),
                entity.isPossuiVp()
        );
    }
}
