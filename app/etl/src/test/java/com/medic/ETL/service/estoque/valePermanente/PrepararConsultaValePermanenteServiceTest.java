package com.medic.ETL.service.estoque.valePermanente;

import com.medic.ETL.dto.empresa.EmpresaValePermanenteDTO;
import com.medic.ETL.repository.empresa.EmpresaRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaValePermanenteServiceTest {

    private final EmpresaRepository repository = mock(EmpresaRepository.class);
    private final PrepararConsultaValePermanenteService service = new PrepararConsultaValePermanenteService(repository);

    @Test
    void shouldBuildPermanentStockQueriesByViman() {
        when(repository.findAllValePermanenteByViman("UFX")).thenReturn(List.of(
                new EmpresaValePermanenteDTO("UFX", "07"),
                new EmpresaValePermanenteDTO("UFX", "08")
        ));
        when(repository.findAllValePermanenteByViman("S00")).thenReturn(List.of(
                new EmpresaValePermanenteDTO("S00", "02")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("'UFX' as Viman"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP07 AS VP"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP08 AS VP"));
        assertFalse(consultas.consultaUfx().contains("VA.VANUME IN"));
        assertTrue(consultas.consultaS00().contains("'S00' as Viman"));
        assertTrue(consultas.consultaS00().contains("FROM SYSADM.VETEVP02 AS VP"));
        assertFalse(consultas.consultaS00().contains("VETEVP07"));
    }
}
