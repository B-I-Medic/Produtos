package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.ETL.model.estoque.segregado.EstoqueSegregado;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.ETL.repository.estoque.segregado.InsercaoConsultaSegregadoProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProcessarEstoqueSegregadoService {

    private final EstoqueSegregadoService estoqueSegregadoService;
    private final ConsultaConsultaSegregadoUFXRepository consultaConsultaSegregadoUFXRepository;
    private final InsercaoConsultaSegregadoProdutoRepository insercaoConsultaSegregadoProdutoRepository;

    public ProcessarEstoqueSegregadoService(EstoqueSegregadoService estoqueSegregadoService,
                                            ConsultaConsultaSegregadoUFXRepository consultaConsultaSegregadoUFXRepository,
                                            InsercaoConsultaSegregadoProdutoRepository insercaoConsultaSegregadoProdutoRepository) {
        this.estoqueSegregadoService = estoqueSegregadoService;
        this.consultaConsultaSegregadoUFXRepository = consultaConsultaSegregadoUFXRepository;
        this.insercaoConsultaSegregadoProdutoRepository = insercaoConsultaSegregadoProdutoRepository;
    }

    public void processarEstoqueSegregado(Processamento processamento) {

        EstoqueSegregadoConsultaDTO consultas = estoqueSegregadoService.montarConsultas(processamento);
        List<EstoqueSegregado> itens = executarConsultaUfx(consultas.consultaUfx());

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

        return consultaConsultaSegregadoUFXRepository.consultar(consulta);
    }
}
