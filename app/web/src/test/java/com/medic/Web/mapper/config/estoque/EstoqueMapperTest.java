package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueMapperTest {

    private final EstoqueSegregadoMapper segregadoMapper = new EstoqueSegregadoMapper();

    @Test
    void shouldMapEstoqueSegregadoForUpdateAndDto() {

        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID empresaMunicipioId = UUID.randomUUID();
        EstoqueSegregadoRequestDTO dto = new EstoqueSegregadoRequestDTO(empresaId, 10, empresaMunicipioId);

        EstoqueSegregadoParametroModel entity = new EstoqueSegregadoParametroModel();
        entity.setId(UUID.randomUUID());
        entity.setCriadoPor(UUID.randomUUID());
        entity.setCriadoEm(Instant.now().minusSeconds(300));

        Instant before = Instant.now();
        EstoqueSegregadoParametroModel mapped = segregadoMapper.toEntity(entity, dto, userId);
        Instant after = Instant.now();

        assertEquals(empresaId, mapped.getIdEmpresa());
        assertEquals(10, mapped.getCodSegregado());
        assertEquals(empresaMunicipioId, mapped.getIdEmpresaMunicipio());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertNotNull(mapped.getAtualizadoEm());
        assertFalse(mapped.getAtualizadoEm().isBefore(before));
        assertFalse(mapped.getAtualizadoEm().isAfter(after));
    }

    @Test
    void shouldMapEstoqueSegregadoForCreate() {

        UUID userId = UUID.randomUUID();
        EstoqueSegregadoParametroModel entity = segregadoMapper.toEntity(
                new EstoqueSegregadoParametroModel(),
                new EstoqueSegregadoRequestDTO(UUID.randomUUID(), 10, UUID.randomUUID()),
                userId);

        assertEquals(userId, entity.getCriadoPor());
    }

}
