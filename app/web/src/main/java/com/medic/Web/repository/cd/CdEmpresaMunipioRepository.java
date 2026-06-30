package com.medic.Web.repository.cd;

import com.medic.Web.model.cd.CdEmpresaMunipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CdEmpresaMunipioRepository extends ReactiveCrudRepository<CdEmpresaMunipioModel, UUID> {

    Flux<CdEmpresaMunipioModel> findByIdEmpresaMunicipio(UUID idEmpresaMunicipio);
}
