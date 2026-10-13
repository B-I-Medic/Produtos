package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.repository.parametro.EstoqueSegregadoParametroRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaEstoqueSegregadoServiceTest {

    private final EstoqueSegregadoParametroRepository repository = mock(EstoqueSegregadoParametroRepository.class);
    private final PrepararConsultaEstoqueSegregadoService service = new PrepararConsultaEstoqueSegregadoService(repository);

    @Test
    void shouldGroupUfxSegregatedCodesByCompanyAndMunicipality() {
        when(repository.obterEstoqueSegregadoParametros()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoParametro("07", "3550308", "10"),
                TestDataFactory.estoqueSegregadoParametro("07", "3550308", "20"),
                TestDataFactory.estoqueSegregadoParametro("07", "3304557", "30")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEES07"));
        assertTrue(consultas.consultaUfx().contains("WHERE ESCDNC IN (10, 20)"));
        assertTrue(consultas.consultaUfx().contains("'3550308' AS CodIBGE"));
        assertTrue(consultas.consultaUfx().contains("'3304557' AS CodIBGE"));
        assertEquals(2, countOccurrences(consultas.consultaUfx(), "FROM SYSADM.VETEES07"));
    }

    private int countOccurrences(String source, String value) {
        return (source.length() - source.replace(value, "").length()) / value.length();
    }
}
