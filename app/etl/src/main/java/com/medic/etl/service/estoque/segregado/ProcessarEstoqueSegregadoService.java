package com.medic.etl.service.estoque.segregado;

import com.medic.etl.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.etl.model.estoque.segregado.EstoqueSegregado;
import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.repository.estoque.segregado.ConsultaConsultaSegregadoS00Repository;
import com.medic.etl.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.etl.repository.estoque.segregado.InsercaoConsultaSegregadoProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProcessarEstoqueSegregadoService {

    private final PrepararConsultaEstoqueSegregadoService prepararConsultaEstoqueSegregadoService;
    private final ConsultaConsultaSegregadoUFXRepository consultaUfxRepository;
    private final ConsultaConsultaSegregadoS00Repository consultaS00Repository;
    private final InsercaoConsultaSegregadoProdutoRepository insercaoConsultaSegregadoProdutoRepository;

    public ProcessarEstoqueSegregadoService(PrepararConsultaEstoqueSegregadoService prepararConsultaEstoqueSegregadoService,
                                            ConsultaConsultaSegregadoUFXRepository consultaUfxRepository,
                                            ConsultaConsultaSegregadoS00Repository consultaS00Repository,
                                            InsercaoConsultaSegregadoProdutoRepository insercaoConsultaSegregadoProdutoRepository) {
        this.prepararConsultaEstoqueSegregadoService = prepararConsultaEstoqueSegregadoService;
        this.consultaUfxRepository = consultaUfxRepository;
        this.consultaS00Repository = consultaS00Repository;
        this.insercaoConsultaSegregadoProdutoRepository = insercaoConsultaSegregadoProdutoRepository;
    }

    public void processarEstoqueSegregado(Processamento processamento) {

        EstoqueSegregadoConsultaDTO consultas = prepararConsultaEstoqueSegregadoService.montarConsultas(processamento);

        List<EstoqueSegregado> itens = new ArrayList<>();
        itens.addAll(executarConsultaUfx(consultas.consultaUfx()));
        itens.addAll(executarConsultaS00(consultas.consultaS00()));

        if (itens.isEmpty()) {
            log.info("Nenhum registro de estoque segregado retornado para o processamento {}.", processamento.getId());
            return;
        }

        insercaoConsultaSegregadoProdutoRepository.inserirEmLote(itens);
        log.info("Processamento {} inseriu {} registros em estoque_segregado.", processamento.getId(), itens.size());
    }

    private List<EstoqueSegregado> executarConsultaUfx(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaUfxRepository.consultar(consulta);
    }

    private List<EstoqueSegregado> executarConsultaS00(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaS00Repository.consultar(consulta);
    }
}
