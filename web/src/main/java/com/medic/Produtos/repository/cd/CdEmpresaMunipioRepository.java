package com.medic.Produtos.repository.cd;

import com.medic.Produtos.model.cd.CdEmpresaMunipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CdEmpresaMunipioRepository extends ReactiveCrudRepository<CdEmpresaMunipioModel, UUID> {

    Flux<CdEmpresaMunipioModel> findByIdEmpresaMunicipio(UUID idEmpresaMunicipio);
}
