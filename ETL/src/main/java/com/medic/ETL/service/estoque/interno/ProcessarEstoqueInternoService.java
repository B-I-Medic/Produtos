package com.medic.ETL.service.estoque.interno;

import com.medic.ETL.dto.consulta.EstoqueInternoConsultaDTO;
import com.medic.ETL.model.estoque.interno.EstoqueInterno;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.estoque.interno.InsercaoEstoqueInternoProdutoRepository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoS00Repository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoUFXRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ProcessarEstoqueInternoService {

    private final EstoqueInternoService estoqueInternoService;
    private final ConsultaEstoqueInternoUFXRepository consultaEstoqueInternoUFXRepository;
    private final ConsultaEstoqueInternoS00Repository consultaEstoqueInternoS00Repository;
    private final InsercaoEstoqueInternoProdutoRepository insercaoEstoqueInternoProdutoRepository;
    private final Executor etlExecutor;

    public ProcessarEstoqueInternoService(EstoqueInternoService estoqueInternoService,
                                          ConsultaEstoqueInternoUFXRepository consultaEstoqueInternoUFXRepository,
                                          ConsultaEstoqueInternoS00Repository consultaEstoqueInternoS00Repository,
                                          InsercaoEstoqueInternoProdutoRepository insercaoEstoqueInternoProdutoRepository,
                                          @Qualifier("etlExecutor") Executor etlExecutor) {
        this.estoqueInternoService = estoqueInternoService;
        this.consultaEstoqueInternoUFXRepository = consultaEstoqueInternoUFXRepository;
        this.consultaEstoqueInternoS00Repository = consultaEstoqueInternoS00Repository;
        this.insercaoEstoqueInternoProdutoRepository = insercaoEstoqueInternoProdutoRepository;
        this.etlExecutor = etlExecutor;
    }

    public void processarEstoqueInterno(Processamento processamento) {

        EstoqueInternoConsultaDTO consultas = estoqueInternoService.montarConsultas(processamento);

        CompletableFuture<List<EstoqueInterno>> consultaUfxFuture = CompletableFuture.supplyAsync(
                () -> executarConsultaUfx(consultas.consultaUfx()),
                etlExecutor
        );

        CompletableFuture<List<EstoqueInterno>> consultaS00Future = CompletableFuture.supplyAsync(
                () -> executarConsultaS00(consultas.consultaS00()),
                etlExecutor
        );

        List<EstoqueInterno> itens = consultaUfxFuture.thenCombine(
                consultaS00Future,
                this::juntarResultados
        ).join();

        if (itens.isEmpty()) {
            log.info("Nenhum registro de estoque interno retornado para o processamento {}.", processamento.getId());
            return;
        }

        insercaoEstoqueInternoProdutoRepository.inserirEmLote(itens);
        log.info("Processamento {} inseriu {} registros em estoque_interno.", processamento.getId(), itens.size());
    }

    private List<EstoqueInterno> executarConsultaUfx(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaEstoqueInternoUFXRepository.consultar(consulta);
    }

    private List<EstoqueInterno> executarConsultaS00(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaEstoqueInternoS00Repository.consultar(consulta);
    }

    private List<EstoqueInterno> juntarResultados(List<EstoqueInterno> itensUfx,
                                                  List<EstoqueInterno> itensS00) {

        List<EstoqueInterno> itens = new ArrayList<>(itensUfx.size() + itensS00.size());
        itens.addAll(itensUfx);
        itens.addAll(itensS00);
        return itens;
    }
}
