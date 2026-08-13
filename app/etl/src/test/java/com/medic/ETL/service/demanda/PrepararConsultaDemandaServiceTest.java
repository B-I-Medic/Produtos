package com.medic.ETL.service.demanda;

import com.medic.ETL.repository.empresa.EmpresaRepository;
import com.medic.ETL.repository.periodo.PeriodoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaDemandaServiceTest {

    private final PeriodoRepository periodoRepository = mock(PeriodoRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final PrepararConsultaDemandaService service = new PrepararConsultaDemandaService(
            periodoRepository,
            empresaRepository
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
                        TestDataFactory.empresa("11")
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
}
