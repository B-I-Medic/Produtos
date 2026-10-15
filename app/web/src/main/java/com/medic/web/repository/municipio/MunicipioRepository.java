package com.medic.web.repository.municipio;

import com.medic.web.model.municipio.MunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface MunicipioRepository extends
        ReactiveCrudRepository<MunicipioModel, UUID>,
        MunicipioRepositoryCustom {
}
