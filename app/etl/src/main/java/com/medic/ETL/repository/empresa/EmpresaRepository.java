package com.medic.ETL.repository.empresa;

import com.medic.ETL.model.empresa.EmpresaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<EmpresaModel, UUID> {

    List<EmpresaModel> findAllByVimanAndPossuiEstoqueInternoIsTrue(String viman);
}
