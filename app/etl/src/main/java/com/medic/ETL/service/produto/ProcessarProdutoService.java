package com.medic.ETL.service.produto;

import com.medic.ETL.dto.consulta.ProdutoConsultaDTO;
import com.medic.ETL.model.produto.Produto;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.produto.ConsultaProdutoRepository;
import com.medic.ETL.repository.produto.InsercaoProdutoProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ProcessarProdutoService {

    private final PrepararConsultaProdutoService prepararConsultaProdutoService;
    private final ConsultaProdutoRepository consultaProdutoRepository;
    private final InsercaoProdutoProdutoRepository insercaoProdutoProdutoRepository;
    private final Executor etlExecutor;

    public ProcessarProdutoService(PrepararConsultaProdutoService prepararConsultaProdutoService,
                                   ConsultaProdutoRepository consultaProdutoRepository,
                                   InsercaoProdutoProdutoRepository insercaoProdutoProdutoRepository,
                                   @Qualifier("etlExecutor") Executor etlExecutor) {
        this.prepararConsultaProdutoService = prepararConsultaProdutoService;
        this.consultaProdutoRepository = consultaProdutoRepository;
        this.insercaoProdutoProdutoRepository = insercaoProdutoProdutoRepository;
        this.etlExecutor = etlExecutor;
    }

    public void atualizarProdutos(Processamento processamento) {

        ProdutoConsultaDTO consultas = prepararConsultaProdutoService.montarConsultas();

        CompletableFuture<List<Produto>> consultaUfxFuture = CompletableFuture.supplyAsync(
                () -> executarConsultaUfx(consultas.consultaUfx()),
                etlExecutor
        );

        CompletableFuture<List<Produto>> consultaS00Future = CompletableFuture.supplyAsync(
                () -> executarConsultaS00(consultas.consultaS00()),
                etlExecutor
        );

        List<Produto> itens = consultaUfxFuture.thenCombine(
                consultaS00Future,
                this::juntarResultados
        ).join();

        if (itens.isEmpty()) {
            log.info("Nenhum registro de produto retornado para o processamento {}.", processamento.getId());
            return;
        }

        insercaoProdutoProdutoRepository.inserirOuAtualizarEmLote(itens);
        log.info("Processamento {} atualizou {} registros em produto.", processamento.getId(), itens.size());
    }

    private List<Produto> executarConsultaUfx(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaProdutoRepository.consultarUFX(consulta);
    }

    private List<Produto> executarConsultaS00(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaProdutoRepository.consultarS00(consulta);
    }

    private List<Produto> juntarResultados(List<Produto> itensUfx, List<Produto> itensS00) {

        List<Produto> itens = new ArrayList<>(itensUfx.size() + itensS00.size());
        itens.addAll(itensUfx);
        itens.addAll(itensS00);
        return itens;
    }
}
