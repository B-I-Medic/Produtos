package com.medic.ETL.service.estoque.valePermanente;

import com.medic.ETL.dto.consulta.ValePermanenteConsultaDTO;
import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaS00Repository;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaUFXRepository;
import com.medic.ETL.repository.estoque.valePermanente.InsercaoValePermanenteProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ProcessarValePermanenteService {

    private final ValePermanenteService valePermanenteService;
    private final ConsultaUFXRepository consultaUFXRepository;
    private final ConsultaS00Repository consultaS00Repository;
    private final InsercaoValePermanenteProdutoRepository insercaoValePermanenteProdutoRepository;
    private final Executor etlExecutor;

    public ProcessarValePermanenteService(ValePermanenteService valePermanenteService,
                                          ConsultaUFXRepository consultaUFXRepository,
                                          ConsultaS00Repository consultaS00Repository,
                                          InsercaoValePermanenteProdutoRepository insercaoValePermanenteProdutoRepository,
                                          @Qualifier("etlExecutor") Executor etlExecutor) {
        this.valePermanenteService = valePermanenteService;
        this.consultaUFXRepository = consultaUFXRepository;
        this.consultaS00Repository = consultaS00Repository;
        this.insercaoValePermanenteProdutoRepository = insercaoValePermanenteProdutoRepository;
        this.etlExecutor = etlExecutor;
    }

    public void processarValePermanente(Processamento processamento) {

        ValePermanenteConsultaDTO consultas = valePermanenteService.montarConsultas(processamento);

        CompletableFuture<List<ValePermanente>> consultaUfxFuture = CompletableFuture.supplyAsync(
                () -> executarConsultaUfx(consultas.consultaUfx()),
                etlExecutor
        );

        CompletableFuture<List<ValePermanente>> consultaS00Future = CompletableFuture.supplyAsync(
                () -> executarConsultaS00(consultas.consultaS00()),
                etlExecutor
        );

        List<ValePermanente> itens = consultaUfxFuture.thenCombine(
                consultaS00Future,
                this::juntarResultados
        ).join();

        if (itens.isEmpty()) {
            log.info("Nenhum registro de vale permanente retornado para o processamento {}.", processamento.getId());
            return;
        }

        insercaoValePermanenteProdutoRepository.inserirEmLote(itens);
        log.info("Processamento {} inseriu {} registros em vale_permanente.", processamento.getId(), itens.size());
    }

    private List<ValePermanente> executarConsultaUfx(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaUFXRepository.consultar(consulta);
    }

    private List<ValePermanente> executarConsultaS00(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaS00Repository.consultar(consulta);
    }

    private List<ValePermanente> juntarResultados(List<ValePermanente> itensUfx,
                                                  List<ValePermanente> itensS00) {

        List<ValePermanente> itens = new ArrayList<>(itensUfx.size() + itensS00.size());
        itens.addAll(itensUfx);
        itens.addAll(itensS00);
        return itens;
    }
}
