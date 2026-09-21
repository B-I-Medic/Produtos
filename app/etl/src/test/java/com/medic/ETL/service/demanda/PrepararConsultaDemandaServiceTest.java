package com.medic.ETL.service.demanda;

import com.medic.ETL.repository.empresa.EmpresaRepository;
import com.medic.ETL.repository.periodo.PeriodoRepository;
import com.medic.ETL.model.periodo.PeriodoTipo;
import com.medic.ETL.service.periodo.PeriodoIntervaloResolver;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaDemandaServiceTest {

    private final PeriodoRepository periodoRepository = mock(PeriodoRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final PrepararConsultaDemandaService service = new PrepararConsultaDemandaService(
            periodoRepository,
            empresaRepository,
            new PeriodoIntervaloResolver(
                    Clock.fixed(
                            Instant.parse("2026-09-21T12:00:00Z"),
                            ZoneId.of("America/Sao_Paulo")
                    )
            )
    );

    @Test
    void shouldMapAllBusinessPeriodsAndCompanyTableSuffixes() {
        when(periodoRepository.findAll()).thenReturn(List.of(
                TestDataFactory.periodo("CIRURGIA", "20260401", "20260430"),
                TestDataFactory.periodo("AGENDAMENTO", "20260301", "20260331"),
                TestDataFactory.periodo("ORCAMENTO_APROVADO", "20260201", "20260228"),
                TestDataFactory.periodo("ORCAMENTO", "20260101", "20260131")
        ));
        when(empresaRepository.findAllByVimanAndPossuiEstoqueInternoIsTrue("UFX"))
                .thenReturn(List.of(
                        TestDataFactory.empresa("07"),
                        TestDataFactory.empresa("08"),
                        TestDataFactory.empresa("11"),
                        TestDataFactory.empresa("12")
                ));

        String sql = service.montarConsulta(TestDataFactory.processamento());

        assertTrue(sql.contains("pv.PVDTCD between 20260101 and 20260131"));
        assertTrue(sql.contains("pv.PVORDT between 20260201 and 20260228"));
        assertTrue(sql.contains("pv.pvdtci between 20260301 and 20260331"));
        assertTrue(sql.contains("va.vadtpv between 20260401 and 20260430"));
        assertTrue(sql.contains("sysadm.vetecl07"));
        assertTrue(sql.contains("sysadm.vetecl08"));
        assertTrue(sql.contains("sysadm.vetecl10"));
        assertFalse(sql.contains("sysadm.vetecl'"));
    }

    @Test
    void shouldCalculateRelativePeriodsAtExecutionTime() {
        when(periodoRepository.findAll()).thenReturn(List.of(
                TestDataFactory.periodo("CIRURGIA", "20260401", "20260430"),
                TestDataFactory.periodoRelativo("AGENDAMENTO", PeriodoTipo.PROXIMOS_DIAS, 7),
                TestDataFactory.periodoRelativo("ORCAMENTO_APROVADO", PeriodoTipo.MESES_FECHADOS, 3),
                TestDataFactory.periodo("ORCAMENTO", "20260101", "20260131")
        ));
        when(empresaRepository.findAllByVimanAndPossuiEstoqueInternoIsTrue("UFX"))
                .thenReturn(List.of(TestDataFactory.empresa("07")));

        String sql = service.montarConsulta(TestDataFactory.processamento());

        assertTrue(sql.contains("pv.PVORDT between 20260601 and 20260831"));
        assertTrue(sql.contains("pv.pvdtci between 20260922 and 20260928"));
    }

    @Test
    void shouldCalculateNextClosedMonthsForAgendamento() {
        when(periodoRepository.findAll()).thenReturn(List.of(
                TestDataFactory.periodo("CIRURGIA", "20260401", "20260430"),
                TestDataFactory.periodoRelativo("AGENDAMENTO", PeriodoTipo.PROXIMOS_MESES_FECHADOS, 3),
                TestDataFactory.periodo("ORCAMENTO_APROVADO", "20260201", "20260228"),
                TestDataFactory.periodo("ORCAMENTO", "20260101", "20260131")
        ));
        when(empresaRepository.findAllByVimanAndPossuiEstoqueInternoIsTrue("UFX"))
                .thenReturn(List.of(TestDataFactory.empresa("07")));

        String sql = service.montarConsulta(TestDataFactory.processamento());

        assertTrue(sql.contains("pv.pvdtci between 20261001 and 20261231"));
    }

    @Test
    void shouldRejectHistoricalRuleForAgendamento() {
        var periodo = TestDataFactory.periodoRelativo("AGENDAMENTO", PeriodoTipo.DIAS, 7);
        when(periodoRepository.findAll()).thenReturn(List.of(periodo));

        assertThrows(IllegalArgumentException.class, () -> service.montarConsulta(TestDataFactory.processamento()));
    }

    @Test
    void shouldRejectIncompleteRelativePeriod() {
        var periodo = TestDataFactory.periodoRelativo("ORCAMENTO", PeriodoTipo.DIAS, 7);
        periodo.setQuantidade(null);
        when(periodoRepository.findAll()).thenReturn(List.of(periodo));

        assertThrows(IllegalStateException.class, () -> service.montarConsulta(TestDataFactory.processamento()));
    }
}
