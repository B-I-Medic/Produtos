package com.medic.etl.service.demanda;

import com.medic.etl.model.demanda.Demanda;
import com.medic.etl.repository.demanda.InsercaoDemandaProdutoRepository;
import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.repository.demanda.ConsultaDemandaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProcessarDemandaService {

    private final ConsultaDemandaRepository consultaDemandaRepository;
    private final PrepararConsultaDemandaService prepararConsultaDemandaService;
    private final InsercaoDemandaProdutoRepository insercaoDemandaProdutoRepository;

    public ProcessarDemandaService(ConsultaDemandaRepository consultaDemandaRepository,
                                   PrepararConsultaDemandaService prepararConsultaDemandaService, InsercaoDemandaProdutoRepository insercaoDemandaProdutoRepository) {
        this.consultaDemandaRepository = consultaDemandaRepository;
        this.prepararConsultaDemandaService = prepararConsultaDemandaService;
        this.insercaoDemandaProdutoRepository = insercaoDemandaProdutoRepository;
    }

    public void atualizarDemanda(Processamento processamento) {

        String consulta = prepararConsultaDemandaService.montarConsulta(processamento);

        List<Demanda> demanda = executarConsultaUfx(consulta);

        if (demanda.isEmpty()) {
            log.info("Nenhum registro de demanda retornado para o processamento {}.", processamento.getId());
            return;
        }

        insercaoDemandaProdutoRepository.inserirEmLote(demanda);
        log.info("Processamento {} inseriu {} registros em demanda.", processamento.getId(), demanda.size());

    }

    private List<Demanda> executarConsultaUfx(String consulta) {

        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        return consultaDemandaRepository.consultarUFX(consulta);
    }
}
