package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaModeloDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoLocalConsulta;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AnvisaRepositoryCustom {

    Mono<AnvisaProdutoDTO> findProduto(String codAnvisa);

    Flux<AnvisaModeloDTO> findModelos(String codAnvisa);

    Flux<EmpresaConsulta> findEmpresas();

    Flux<AnvisaProdutoLocalConsulta> findProdutosLocais(String codAnvisa);

    record EmpresaConsulta(
            String descricao,
            String viman,
            String codigoEmpresa
    ) {
    }
}
