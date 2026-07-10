package com.medic.Web.repository.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoResponseDTO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NecessidadeRepositoryCustom {

    Flux<NecessidadeAgrupadoResponseDTO> findByFilter(NecessidadeFilterDTO filter);
}
