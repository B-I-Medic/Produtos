package com.medic.etl.service.estoque.segregado;

import com.medic.etl.repository.catalogo.EstoqueSegregadoCatalogoRepository;
import com.medic.etl.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepararConsultaEstoqueSegregadoServiceTest {

    private final EstoqueSegregadoCatalogoRepository repository = mock(EstoqueSegregadoCatalogoRepository.class);
    private final PrepararConsultaEstoqueSegregadoService service = new PrepararConsultaEstoqueSegregadoService(repository);

    @Test
    void shouldGroupUfxSegregatedCodesByCompanyAndMunicipality() {
        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", "3550308", "10"),
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", "3550308", "20"),
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", "3304557", "30")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().contains("FROM SYSADM.VETEES07"));
        assertTrue(consultas.consultaUfx().contains("WHERE ESCDNC IN (10, 20)"));
        assertTrue(consultas.consultaUfx().contains("'3550308' AS CodIBGE"));
        assertTrue(consultas.consultaUfx().contains("'3304557' AS CodIBGE"));
        assertEquals(2, countOccurrences(consultas.consultaUfx(), "FROM SYSADM.VETEES07"));
        assertTrue(consultas.consultaS00().isBlank());
    }

    @Test
    void shouldBuildS00QueryAndNormalizeNumericCodes() {
        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("S00", "02", "3304557", "010")
        ));

        var consultas = service.montarConsultas(TestDataFactory.processamento());

        assertTrue(consultas.consultaUfx().isBlank());
        assertTrue(consultas.consultaS00().contains("FROM SYSADM.VETEES02"));
        assertTrue(consultas.consultaS00().contains("WHERE ESCDNC IN (10)"));
    }

    @Test
    void shouldRejectInvalidCompanyCode() {
        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "ABC", "3550308", "10")
        ));

        assertThrows(IllegalArgumentException.class,
                () -> service.montarConsultas(TestDataFactory.processamento()));

        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", null, "3550308", "10")
        ));
        assertThrows(IllegalArgumentException.class,
                () -> service.montarConsultas(TestDataFactory.processamento()));
    }

    @Test
    void shouldRejectInvalidMunicipalityCode() {
        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", "not-ibge", "10")
        ));

        assertThrows(IllegalArgumentException.class,
                () -> service.montarConsultas(TestDataFactory.processamento()));

        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", null, "10")
        ));
        assertThrows(IllegalArgumentException.class,
                () -> service.montarConsultas(TestDataFactory.processamento()));
    }

    @Test
    void shouldRejectNonNumericSegregatedCode() {
        when(repository.obterEstoqueSegregadoCatalogo()).thenReturn(List.of(
                TestDataFactory.estoqueSegregadoCatalogo("UFX", "07", "3550308", "bad")
        ));

        assertThrows(IllegalArgumentException.class,
                () -> service.montarConsultas(TestDataFactory.processamento()));
    }

    private int countOccurrences(String source, String value) {
        return (source.length() - source.replace(value, "").length()) / value.length();
    }
}
