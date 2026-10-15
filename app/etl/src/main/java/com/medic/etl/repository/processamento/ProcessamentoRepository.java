package com.medic.etl.repository.processamento;

import com.medic.etl.model.processamento.Processamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessamentoRepository extends
        JpaRepository<Processamento, UUID>,
        ProcessamentoCustomRepository {
}
