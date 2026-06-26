package com.medic.Produtos.repository.empresa;

import com.medic.Produtos.model.empresa.EmpresaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EmpresaRepository extends ReactiveCrudRepository<EmpresaModel, UUID> {
}
