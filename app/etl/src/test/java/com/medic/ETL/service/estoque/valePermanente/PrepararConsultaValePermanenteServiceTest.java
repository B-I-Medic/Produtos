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
                new EmpresaValePermanenteDTO("UFX", "01"),
                new EmpresaValePermanenteDTO("UFX", "07"),
                new EmpresaValePermanenteDTO("UFX", "08"),
                new EmpresaValePermanenteDTO("UFX", "11"),
                new EmpresaValePermanenteDTO("UFX", "09")
        ));
        when(repository.findAllValePermanenteByViman("S00")).thenReturn(List.of(
                new EmpresaValePermanenteDTO("S00", "02"),
                new EmpresaValePermanenteDTO("S00", "10"),
                new EmpresaValePermanenteDTO("S00", "21")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("'UFX' as Viman"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP07 AS VP"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP08 AS VP"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEVP01 AS VP"));
        assertTrue(consultas.consultaUfx().contains("JOIN SYSADM.VETECL10 AS CL"));
        assertFalse(consultas.consultaUfx().contains("FROM SYSADM.VETEVP09"));
        assertFalse(consultas.consultaUfx().contains("VA.VANUME IN"));
        assertTrue(consultas.consultaS00().contains("'S00' as Viman"));
        assertTrue(consultas.consultaS00().contains("FROM SYSADM.VETEVP02 AS VP"));
        assertTrue(consultas.consultaS00().contains("FROM SYSADM.VETEVP21 AS VP"));
        assertTrue(consultas.consultaS00().contains("JOIN SYSADM.VETECL01 AS CL"));
        assertTrue(consultas.consultaS00().contains("JOIN SYSADM.VETECL10 AS CL"));
        assertFalse(consultas.consultaS00().contains("VETEVP07"));
    }

    @Test
    void shouldReturnBlankQueriesWhenThereAreNoCompanies() {
        when(repository.findAllValePermanenteByViman("UFX")).thenReturn(List.of());
        when(repository.findAllValePermanenteByViman("S00")).thenReturn(List.of());

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().isBlank());
        assertTrue(consultas.consultaS00().isBlank());
    }
}
