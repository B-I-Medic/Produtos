package com.medic.Web.mapper.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeResponseDTO;
import com.medic.Web.model.necessidade.NecessidadeModel;
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
