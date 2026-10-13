package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import com.medic.Web.model.municipio.MunicipioModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueMapperTest {

    private final EstoqueSegregadoMapper segregadoMapper = new EstoqueSegregadoMapper();

    @Test
    void shouldMapEstoqueSegregadoForUpdateWithMunicipalityCode() {

        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        EstoqueSegregadoRequestDTO dto = new EstoqueSegregadoRequestDTO(empresaId, 10, UUID.randomUUID());
        MunicipioModel municipio = new MunicipioModel();
        municipio.setCodIbge("3550308");

        EstoqueSegregadoParametroModel entity = new EstoqueSegregadoParametroModel();
        entity.setId(UUID.randomUUID());
        entity.setCriadoPor(UUID.randomUUID());
        entity.setCriadoEm(Instant.now().minusSeconds(300));

        Instant before = Instant.now();
        EstoqueSegregadoParametroModel mapped = segregadoMapper.toEntity(entity, dto, municipio, userId);
        Instant after = Instant.now();

        assertEquals(empresaId, mapped.getIdEmpresa());
        assertEquals(10, mapped.getCodSegregado());
        assertEquals("3550308", mapped.getCodIBGE());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertNotNull(mapped.getAtualizadoEm());
        assertFalse(mapped.getAtualizadoEm().isBefore(before));
        assertFalse(mapped.getAtualizadoEm().isAfter(after));
    }

    @Test
    void shouldMapEstoqueSegregadoForCreate() {

        UUID userId = UUID.randomUUID();
        MunicipioModel municipio = new MunicipioModel();
        municipio.setCodIbge("3304557");
        EstoqueSegregadoParametroModel entity = segregadoMapper.toEntity(
                new EstoqueSegregadoParametroModel(),
                new EstoqueSegregadoRequestDTO(UUID.randomUUID(), 10, UUID.randomUUID()),
                municipio,
                userId);

        assertEquals(userId, entity.getCriadoPor());
        assertEquals("3304557", entity.getCodIBGE());
    }

}
