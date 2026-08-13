package com.medic.ETL.service.produto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrepararConsultaProdutoServiceTest {

    private final PrepararConsultaProdutoService service = new PrepararConsultaProdutoService();

    @Test
    void shouldBuildUfxAndS00QueriesWithTrimmedCompanyCodes() {
        var consultas = service.montarConsultas();

        assertTrue(consultas.consultaUfx().contains("trim('01,03,04,05,06,13') AS CodEmpresa"));
        assertTrue(consultas.consultaUfx().contains("trim('07') AS CodEmpresa"));
        assertTrue(consultas.consultaS00().contains("trim('12') AS CodEmpresa"));
        assertFalse(consultas.consultaUfx().contains("'07 ' AS CodEmpresa"));
        assertFalse(consultas.consultaS00().contains("'12 ' AS CodEmpresa"));
    }
}
