package com.medic.Web.mapper.empresa;

import com.medic.Web.model.empresa.EmpresaMunicipioModel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmpresaMunicipioMapperTest {

    private final EmpresaMunicipioMapper mapper = new EmpresaMunicipioMapper();

    @Test
    void shouldMapEmpresaMunicipioForCreate() {

        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var entity = mapper.toEntity(new EmpresaMunicipioModel(), empresaId, municipioId, userId);

        assertEquals(empresaId, entity.getIdEmpresa());
        assertEquals(municipioId, entity.getIdMunicipio());
        assertEquals(userId, entity.getCriadoPor());
    }

}
