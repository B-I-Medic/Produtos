package com.medic.Web.repository.cd;

import com.medic.Web.model.municipio.MunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface MunicipioRepository extends
        ReactiveCrudRepository<MunicipioModel, UUID>,
        MunicipioRepositoryCustom {
}
