package com.medic.Web.repository.empresa;

import com.medic.Web.model.empresa.EmpresaMunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EmpresaMunicipioRepository extends
        ReactiveCrudRepository<EmpresaMunicipioModel, UUID>,
        EmpresaMunipioRepositoryCustom {
}
