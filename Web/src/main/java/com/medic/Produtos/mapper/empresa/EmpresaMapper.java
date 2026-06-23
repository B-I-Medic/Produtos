package com.medic.Produtos.mapper.empresa;

import com.medic.Produtos.dto.empresa.EmpresaRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaResponseDTO;
import com.medic.Produtos.model.empresa.EmpresaModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EmpresaMapper {

    public EmpresaModel toEntity(EmpresaModel entity,
                                 EmpresaRequestDTO dto,
                                 UUID userId) {

        entity.setDescricao(dto.descricao());
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

        return new EmpresaResponseDTO(
                entity.getId(),
                entity.getDescricao(),
                entity.getViman(),
                entity.getCodigoEmpresa(),
                entity.isPossuiEstoqueInterno(),
                entity.isPossuiEstoqueSegregado(),
                entity.isPossuiVp()
        );
    }
}
