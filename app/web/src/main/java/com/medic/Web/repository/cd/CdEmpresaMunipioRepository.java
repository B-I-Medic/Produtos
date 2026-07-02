package com.medic.Web.repository.cd;

import com.medic.Web.model.cd.CdEmpresaMunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CdEmpresaMunipioRepository extends
        ReactiveCrudRepository<CdEmpresaMunicipioModel, UUID>,
        CdEmpresaMunipioRepositoryCustom {
}
