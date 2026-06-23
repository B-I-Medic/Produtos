package com.medic.Produtos.mapper.necessidade;

import com.medic.Produtos.dto.necessidade.NecessidadeResponseDTO;
import com.medic.Produtos.model.necessidade.NecessidadeModel;
import org.springframework.stereotype.Component;

@Component
public class NecessidadeMapper {

    public NecessidadeResponseDTO toDTO(NecessidadeModel entity) {

        return new NecessidadeResponseDTO(
                entity.getId(),
                entity.getIdEmpresaMunicipio(),
                entity.getCodProduto(),
                entity.getEstoque(),
                entity.getDemanda(),
                entity.getNecessidade()
        );
    }
}
