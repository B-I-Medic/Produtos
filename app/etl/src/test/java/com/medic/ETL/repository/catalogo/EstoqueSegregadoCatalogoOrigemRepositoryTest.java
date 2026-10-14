package com.medic.ETL.repository.catalogo;

import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import com.medic.ETL.model.empresa.EmpresaModel;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstoqueSegregadoCatalogoOrigemRepositoryTest {

    private final JdbcTemplate ufx = mock(JdbcTemplate.class);
    private final JdbcTemplate s00 = mock(JdbcTemplate.class);
    private final EstoqueSegregadoCatalogoOrigemRepository repository =
            new EstoqueSegregadoCatalogoOrigemRepository(ufx, s00);

    @Test
    void shouldQueryUfxCatalogAndMapTrimmedDescription() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("CODIGO")).thenReturn(42);
        when(resultSet.getString("DESCRICAO")).thenReturn("  MATERIAL  ");
        when(ufx.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });
        EmpresaModel empresa = empresa("01");

        List<EstoqueSegregadoCatalogoItemDTO> itens = repository.consultarUfx(empresa);

        assertEquals(new EstoqueSegregadoCatalogoItemDTO(empresa.getId(), 42, "MATERIAL"), itens.getFirst());
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(ufx).query(sql.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("FROM SYSADM.VETEES01 ES"));
        assertTrue(sql.getValue().contains("JOIN SYSADM.VETENC01 NC"));
    }

    @Test
    void shouldUseEverySupportedUfxVeteencSuffix() {
        assertUfxSuffix("07", "07");
        assertUfxSuffix("08", "08");
        assertUfxSuffix("11", "10");
        assertUfxSuffix("13", "01");
    }

    @Test
    void shouldQueryS00UsingWrappedCompanySuffixAndMapRows() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("CODIGO")).thenReturn(7);
        when(resultSet.getString("DESCRICAO")).thenReturn(" Estoque ");
        when(s00.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });
        EmpresaModel empresa = empresa("21");

        List<EstoqueSegregadoCatalogoItemDTO> itens = repository.consultarS00(empresa);

        assertEquals(new EstoqueSegregadoCatalogoItemDTO(empresa.getId(), 7, "Estoque"), itens.getFirst());
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(s00).query(sql.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("FROM SYSADM.VETEES21 ES"));
        assertTrue(sql.getValue().contains("JOIN SYSADM.VETENC01 NC"));
    }

    @Test
    void shouldRejectInvalidCompanyCodesAndUnmappedUfxCompanies() {
        assertThrows(IllegalArgumentException.class, () -> repository.consultarUfx(empresa(null)));
        assertThrows(IllegalArgumentException.class, () -> repository.consultarS00(empresa(" A ")));
        assertThrows(IllegalArgumentException.class, () -> repository.consultarS00(empresa("100")));
        assertThrows(IllegalArgumentException.class, () -> repository.consultarUfx(empresa("02")));
    }

    private void assertUfxSuffix(String codigoEmpresa, String sufixoVetenc) {
        repository.consultarUfx(empresa(codigoEmpresa));
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(ufx, org.mockito.Mockito.atLeastOnce()).query(sql.capture(), any(RowMapper.class));
        assertTrue(sql.getAllValues().getLast().contains("JOIN SYSADM.VETENC" + sufixoVetenc + " NC"));
    }

    private EmpresaModel empresa(String codigoEmpresa) {
        EmpresaModel empresa = new EmpresaModel();
        empresa.setId(java.util.UUID.randomUUID());
        empresa.setCodigoEmpresa(codigoEmpresa);
        return empresa;
    }
}
