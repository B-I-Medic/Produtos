package com.medic.ETL.service.estoque.valePermanente;

import com.medic.ETL.repository.parametro.ValePermanenteParametroRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaValePermanenteServiceTest {

    private final ValePermanenteParametroRepository repository = mock(ValePermanenteParametroRepository.class);
    private final PrepararConsultaValePermanenteService service = new PrepararConsultaValePermanenteService(repository);

    @Test
    void shouldSplitPermanentStockQueriesByViman() {
        UUID ufxEmpresaMunicipio = UUID.fromString("00000000-0000-0000-0000-000000000401");
        UUID s00EmpresaMunicipio = UUID.fromString("00000000-0000-0000-0000-000000000402");
        when(repository.obterValePermanenteParametros()).thenReturn(List.of(
                TestDataFactory.valePermanenteParametro(ufxEmpresaMunicipio, "UFX", "07", "100"),
                TestDataFactory.valePermanenteParametro(ufxEmpresaMunicipio, "UFX", "07", "200"),
                TestDataFactory.valePermanenteParametro(s00EmpresaMunicipio, "S00", "02", "300")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("'UFX' as Viman"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP07 AS VP"));
        assertTrue(consultas.consultaUfx().contains("VA.VANUME IN (100, 200)"));
        assertTrue(consultas.consultaS00().contains("'S00' as Viman"));
        assertTrue(consultas.consultaS00().contains("FROM SYSADM.VETEVP02 AS VP"));
        assertFalse(consultas.consultaS00().contains("VETEVP07"));
    }
}
