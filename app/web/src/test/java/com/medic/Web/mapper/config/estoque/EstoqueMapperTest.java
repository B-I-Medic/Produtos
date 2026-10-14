package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueSegregadoCatalogoModel;
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

        EstoqueSegregadoCatalogoModel entity = new EstoqueSegregadoCatalogoModel();
        entity.setId(UUID.randomUUID());

        Instant before = Instant.now();
        EstoqueSegregadoCatalogoModel mapped = segregadoMapper.toEntity(entity, dto, municipio, userId);
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
        EstoqueSegregadoCatalogoModel entity = segregadoMapper.toEntity(
                new EstoqueSegregadoCatalogoModel(),
                new EstoqueSegregadoRequestDTO(UUID.randomUUID(), 10, UUID.randomUUID()),
                municipio,
                userId);

        assertEquals("3304557", entity.getCodIBGE());
        assertNull(entity.getAtualizadoPor());
        assertNull(entity.getAtualizadoEm());
    }

}
