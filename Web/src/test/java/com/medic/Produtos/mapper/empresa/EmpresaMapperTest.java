package com.medic.Produtos.mapper.empresa;

import com.medic.Produtos.dto.empresa.EmpresaRequestDTO;
import com.medic.Produtos.model.empresa.EmpresaModel;
import com.medic.Produtos.model.empresa.Viman;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaMapperTest {

    private final EmpresaMapper mapper = new EmpresaMapper();

    @Test
    void shouldMapEmpresaForCreate() {

        UUID userId = UUID.randomUUID();
        EmpresaRequestDTO dto = new EmpresaRequestDTO("Empresa A", Viman.UFX, "001", true, false, true);

        EmpresaModel entity = mapper.toEntity(new EmpresaModel(), dto, userId);

        assertEquals("Empresa A", entity.getDescricao());
        assertEquals(Viman.UFX, entity.getViman());
        assertEquals("001", entity.getCodigoEmpresa());
        assertTrue(entity.isPossuiEstoqueInterno());
        assertFalse(entity.isPossuiEstoqueSegregado());
        assertTrue(entity.isPossuiVp());
        assertEquals(userId, entity.getCriadoPor());
        assertNull(entity.getAtualizadoPor());
        assertNull(entity.getAtualizadoEm());
    }

    @Test
    void shouldMapEmpresaForUpdate() {

        UUID userId = UUID.randomUUID();
        EmpresaRequestDTO dto = new EmpresaRequestDTO("Empresa B", Viman.S00, "002", false, true, false);
        EmpresaModel current = new EmpresaModel();
        current.setId(UUID.randomUUID());
        current.setCriadoPor(UUID.randomUUID());
        current.setCriadoEm(Instant.now().minusSeconds(3600));

        Instant before = Instant.now();
        EmpresaModel entity = mapper.toEntity(current, dto, userId);
        Instant after = Instant.now();

        assertEquals("Empresa B", entity.getDescricao());
        assertEquals(Viman.S00, entity.getViman());
        assertEquals("002", entity.getCodigoEmpresa());
        assertFalse(entity.isPossuiEstoqueInterno());
        assertTrue(entity.isPossuiEstoqueSegregado());
        assertFalse(entity.isPossuiVp());
        assertEquals(userId, entity.getAtualizadoPor());
        assertNotNull(entity.getAtualizadoEm());
        assertFalse(entity.getAtualizadoEm().isBefore(before));
        assertFalse(entity.getAtualizadoEm().isAfter(after));
    }

    @Test
    void shouldMapEmpresaToResponseDto() {

        EmpresaModel entity = new EmpresaModel();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setDescricao("Empresa C");
        entity.setViman(Viman.UFX);
        entity.setCodigoEmpresa("003");
        entity.setPossuiEstoqueInterno(true);
        entity.setPossuiEstoqueSegregado(true);
        entity.setPossuiVp(false);

        var dto = mapper.toDTO(entity);

        assertEquals(id, dto.id());
        assertEquals("Empresa C", dto.descricao());
        assertEquals(Viman.UFX, dto.viman());
        assertEquals("003", dto.codigoEmpresa());
        assertTrue(dto.possuiEstoqueInterno());
        assertTrue(dto.possuiEstoqueSegregado());
        assertFalse(dto.possuiVp());
    }
}
