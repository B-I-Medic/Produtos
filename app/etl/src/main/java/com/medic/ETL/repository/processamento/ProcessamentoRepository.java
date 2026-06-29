package com.medic.ETL.repository.processamento;

import com.medic.ETL.model.processamento.Processamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessamentoRepository extends JpaRepository<Processamento, UUID> {
}
