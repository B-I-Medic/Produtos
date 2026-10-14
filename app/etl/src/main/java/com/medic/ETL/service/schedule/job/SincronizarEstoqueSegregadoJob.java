package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.service.estoque.segregado.SincronizarEstoqueSegregadoCatalogoService;
import com.medic.ETL.service.schedule.RegistrarExecucaoScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class SincronizarEstoqueSegregadoJob implements Job {

    private final ReentrantLock lock = new ReentrantLock();
    private final SincronizarEstoqueSegregadoCatalogoService sincronizarCatalogoService;
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService;

    public SincronizarEstoqueSegregadoJob(SincronizarEstoqueSegregadoCatalogoService sincronizarCatalogoService,
                                          RegistrarExecucaoScheduleService registrarExecucaoScheduleService) {
        this.sincronizarCatalogoService = sincronizarCatalogoService;
        this.registrarExecucaoScheduleService = registrarExecucaoScheduleService;
    }

    @Override
    public ScheduleJob getJob() {
        return ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO;
    }

    @Override
    public void run() {

        if (!lock.tryLock()) {
            log.info("Sincronizacao do catalogo de estoque segregado ja esta em execucao.");
            return;
        }

        try {

            registrarExecucaoScheduleService.registrarInicio(getJob());
            sincronizarCatalogoService.sincronizar();

        } catch (RuntimeException exception) {
            log.error("Falha ao sincronizar o catalogo de estoque segregado.", exception);
            throw exception;
        } finally {
            lock.unlock();
        }
    }
}
