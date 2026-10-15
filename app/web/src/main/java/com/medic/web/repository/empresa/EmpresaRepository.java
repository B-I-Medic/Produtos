package com.medic.web.repository.empresa;

import com.medic.web.model.empresa.EmpresaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EmpresaRepository extends
        ReactiveCrudRepository<EmpresaModel, UUID>,
        EmpresaRepositoryCustom
{
}
