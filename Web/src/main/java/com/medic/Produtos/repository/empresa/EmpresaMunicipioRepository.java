package com.medic.Produtos.repository.empresa;

import com.medic.Produtos.model.empresa.EmpresaMunicipioModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EmpresaMunicipioRepository extends ReactiveCrudRepository<EmpresaMunicipioModel, UUID> {
}
