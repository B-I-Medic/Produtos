package com.medic.Web.repository.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoPorCDResponseDTO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NecessidadeRepositoryCustom {

    Flux<NecessidadeAgrupadoPorCDResponseDTO> findByCDFilter(NecessidadeFilterDTO filter);
}
