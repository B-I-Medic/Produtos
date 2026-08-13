package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.repository.parametro.EstoqueSegregadoParametroRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaEstoqueSegregadoServiceTest {

    private final EstoqueSegregadoParametroRepository repository = mock(EstoqueSegregadoParametroRepository.class);
    private final PrepararConsultaEstoqueSegregadoService service = new PrepararConsultaEstoqueSegregadoService(repository);

    @Test
    void shouldGroupUfxSegregatedCodesByCompanyAndMunicipality() {
        UUID empresaMunicipio = UUID.fromString("00000000-0000-0000-0000-000000000301");
        when(repository.obterEstoqueSegregadoParametros()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoParametro(empresaMunicipio, "UFX", "07", "10"),
                TestDataFactory.estoqueSegregadoParametro(empresaMunicipio, "UFX", "07", "20"),
                TestDataFactory.estoqueSegregadoParametro(UUID.randomUUID(), "S00", "01", "30")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEES07"));
        assertTrue(consultas.consultaUfx().contains("WHERE ESCDNC IN (10, 20)"));
        assertTrue(consultas.consultaUfx().contains(empresaMunicipio.toString()));
        assertFalse(consultas.consultaUfx().contains("VETEES01"));
    }
}
