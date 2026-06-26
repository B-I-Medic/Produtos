package com.medic.Produtos.repository.cd;

import com.medic.Produtos.model.municipio.MunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface MunicipioRepository extends ReactiveCrudRepository<MunicipioModel, UUID> {
}
