package com.medic.ETL.service.estoque.interno;

import com.medic.ETL.repository.parametro.EstoqueInternoParametroRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaEstoqueInternoServiceTest {

    private final EstoqueInternoParametroRepository repository = mock(EstoqueInternoParametroRepository.class);
    private final PrepararConsultaEstoqueInternoService service = new PrepararConsultaEstoqueInternoService(repository);

    @Test
    void shouldGroupGenericUfxCodesAndUseSpecialTables() {
        UUID empresaMunicipio = UUID.fromString("00000000-0000-0000-0000-000000000201");
        when(repository.obterEstoqueInternoParametros()).thenReturn(List.of(
                TestDataFactory.estoqueInternoParametro(empresaMunicipio, "UFX", "01"),
                TestDataFactory.estoqueInternoParametro(empresaMunicipio, "UFX", "03"),
                TestDataFactory.estoqueInternoParametro(empresaMunicipio, "UFX", "07"),
                TestDataFactory.estoqueInternoParametro(empresaMunicipio, "UFX", "08"),
                TestDataFactory.estoqueInternoParametro(empresaMunicipio, "S00", "02")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("PU.PUCDUS IN (1, 3)"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEPR07"));
        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEPR08"));
        assertTrue(consultas.consultaS00().contains("PU.PUCDUS IN (2)"));
        assertFalse(consultas.consultaS00().contains("VETEPR07"));
    }
}
