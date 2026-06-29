package com.medic.Web.repository.empresa;

import com.medic.Web.model.empresa.EmpresaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EmpresaRepository extends ReactiveCrudRepository<EmpresaModel, UUID> {
}
