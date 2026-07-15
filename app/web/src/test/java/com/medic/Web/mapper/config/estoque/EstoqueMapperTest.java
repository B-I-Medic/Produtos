package com.medic.Web.mapper.config.estoque;

import com.medic.Web.dto.config.estoque.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.ValePermanenteRequestDTO;
import com.medic.Web.model.config.estoque.EstoqueInternoParametroModel;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import com.medic.Web.model.config.estoque.ValePermanenteParametroModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueMapperTest {

    private final EstoqueInternoMapper internoMapper = new EstoqueInternoMapper();
    private final EstoqueSegregadoMapper segregadoMapper = new EstoqueSegregadoMapper();
    private final ValePermanenteMapper valeMapper = new ValePermanenteMapper();

    @Test
    void shouldMapEstoqueInternoForCreateAndDto() {

        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID empresaMunicipioId = UUID.randomUUID();
        EstoqueInternoRequestDTO dto = new EstoqueInternoRequestDTO(empresaId, empresaMunicipioId);

        EstoqueInternoParametroModel entity = internoMapper.toEntity(new EstoqueInternoParametroModel(), dto, userId);
        var response = internoMapper.toDTO(entity);

        assertEquals(empresaId, entity.getIdEmpresa());
        assertEquals(empresaMunicipioId, entity.getIdEmpresaMunicipio());
        assertEquals(userId, entity.getCriadoPor());
        assertNull(entity.getAtualizadoPor());
        assertEquals(empresaId, response.idEmpresa());
        assertEquals(empresaMunicipioId, response.id_empresa_municipio());
    }

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
        var response = segregadoMapper.toDTO(mapped);

        assertEquals(empresaId, mapped.getIdEmpresa());
        assertEquals(10, mapped.getCodSegregado());
        assertEquals(empresaMunicipioId, mapped.getIdEmpresaMunicipio());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertNotNull(mapped.getAtualizadoEm());
        assertFalse(mapped.getAtualizadoEm().isBefore(before));
        assertFalse(mapped.getAtualizadoEm().isAfter(after));
        assertEquals(10, response.codSegregado());
    }

    @Test
    void shouldMapValePermanenteForCreateAndDto() {

        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID empresaMunicipioId = UUID.randomUUID();
        ValePermanenteRequestDTO dto = new ValePermanenteRequestDTO(empresaId, 20, empresaMunicipioId);

        ValePermanenteParametroModel entity = valeMapper.toEntity(new ValePermanenteParametroModel(), dto, userId);
        var response = valeMapper.toDTO(entity);

        assertEquals(empresaId, entity.getIdEmpresa());
        assertEquals(20, entity.getCodVp());
        assertEquals(empresaMunicipioId, entity.getIdEmpresaMunicipio());
        assertEquals(userId, entity.getCriadoPor());
        assertEquals(empresaId, response.idEmpresa());
        assertEquals(20, response.codVp());
        assertEquals(empresaMunicipioId, response.id_empresa_municipio());
    }
}
