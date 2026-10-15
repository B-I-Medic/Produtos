package com.medic.etl.service.estoque.interno;

import com.medic.etl.dto.empresa.EmpresaEstoqueInternoDTO;
import com.medic.etl.repository.empresa.EmpresaRepository;
import com.medic.etl.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaEstoqueInternoServiceTest {

    private final EmpresaRepository repository = mock(EmpresaRepository.class);
    private final PrepararConsultaEstoqueInternoService service = new PrepararConsultaEstoqueInternoService(repository);

    @Test
    void shouldBuildQueriesUsingJoinedMunicipalityData() {
        when(repository.findAllEstoqueInternoByViman("UFX")).thenReturn(List.of(
                new EmpresaEstoqueInternoDTO("S00", "09", "wrong-source"),
                new EmpresaEstoqueInternoDTO("UFX", "01", "3550308"),
                new EmpresaEstoqueInternoDTO("UFX", "03", "3550308"),
                new EmpresaEstoqueInternoDTO("UFX", "07", "3550308"),
                new EmpresaEstoqueInternoDTO("UFX", "08", "3550308"),
                new EmpresaEstoqueInternoDTO("UFX", "09", "3550308")
        ));
        when(repository.findAllEstoqueInternoByViman("S00")).thenReturn(List.of(
                new EmpresaEstoqueInternoDTO("UFX", "09", "wrong-source"),
                new EmpresaEstoqueInternoDTO("S00", "02", "3304557")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("PU.PUCDUS IN (01)"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEPR07"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEPR08"));
        assertTrue(consultas.consultaUfx().contains("'3550308' AS CodIBGE"));
        assertTrue(consultas.consultaS00().contains("PU.PUCDUS IN (02)"));
        assertTrue(consultas.consultaS00().contains("'3304557' AS CodIBGE"));
        assertFalse(consultas.consultaS00().contains("VETEPR07"));
    }

    @Test
    void shouldReturnBlankQueriesWhenSourceRowsDoNotMatchTheirViman() {
        when(repository.findAllEstoqueInternoByViman("UFX")).thenReturn(List.of(
                new EmpresaEstoqueInternoDTO("S00", "02", "3304557")
        ));
        when(repository.findAllEstoqueInternoByViman("S00")).thenReturn(List.of(
                new EmpresaEstoqueInternoDTO("UFX", "01", "3550308")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().isBlank());
        assertTrue(consultas.consultaS00().isBlank());
    }
}
