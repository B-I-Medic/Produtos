package com.medic.Web.support;

import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.Web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.Web.dto.config.taxa.TaxaResponseDTO;
import com.medic.Web.dto.usuario.UsuarioResponseDTO;
import com.medic.Web.model.auth.PasswordResetCodeModel;
import com.medic.Web.model.cd.CentroDistribuicaoModel;
import com.medic.Web.model.municipio.MunicipioModel;
import com.medic.Web.model.empresa.EmpresaModel;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import com.medic.Web.model.config.periodo.PeriodoEnum;
import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.model.config.taxa.TaxaEnum;
import com.medic.Web.model.config.taxa.TaxaModel;
import com.medic.Web.model.usuario.Role;
import com.medic.Web.model.usuario.UsuarioModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UsuarioModel usuarioModel() {

        var model = new UsuarioModel();
        model.setId(UUID.randomUUID());
        model.setNome("Teste");
        model.setEmail("teste@medic.com");
        model.setSenha("hash");
        model.setRole(Role.ADMIN);
        model.setAtivo(true);
        model.setPrimeiroAcesso(true);
        return model;
    }

    public static UsuarioResponseDTO usuarioResponseDTO() {

        var user = usuarioModel();
        return new UsuarioResponseDTO(user.getId(), user.getNome(), user.getEmail(), user.getRole(), user.getAtivo());
    }

    public static PeriodoModel periodoModel() {

        var model = new PeriodoModel();
        model.setId(UUID.randomUUID());
        model.setDescricao(PeriodoEnum.ORCAMENTO);
        model.setDataInicial(LocalDate.now());
        model.setDataFinal(LocalDate.now().plusDays(1));
        return model;
    }

    public static PeriodoResponseDTO periodoResponseDTO() {

        var model = periodoModel();
        return new PeriodoResponseDTO(
                model.getId(),
                model.getDescricao(),
                model.getTipoPeriodo(),
                model.getQuantidade(),
                model.getDataInicial(),
                model.getDataFinal()
        );
    }

    public static TaxaModel taxaModel() {

        var model = new TaxaModel();
        model.setId(UUID.randomUUID());
        model.setDescricao(TaxaEnum.ORCAMENTO);
        model.setTaxa(BigDecimal.TEN);
        return model;
    }

    public static TaxaResponseDTO taxaResponseDTO() {

        var model = taxaModel();
        return new TaxaResponseDTO(model.getId(), model.getDescricao(), model.getTaxa());
    }

    public static CentroDistribuicaoModel centroDistribuicaoModel() {

        var model = new CentroDistribuicaoModel();
        model.setId(UUID.randomUUID());
        model.setDescricao("CD");
        model.setMunicipioId(UUID.randomUUID());
        model.setCriadoPor(UUID.randomUUID());
        return model;
    }

    public static CentroDistribuicaoResponseDTO centroDistribuicaoResponseDTO() {

        var model = centroDistribuicaoModel();
        return new CentroDistribuicaoResponseDTO(
                model.getId(),
                model.getDescricao(),
                new MunicipioResponseDTO(model.getMunicipioId(), "Cidade", "123", "SP")
        );
    }

    public static EmpresaModel empresaModel() {

        var model = new EmpresaModel();
        model.setId(UUID.randomUUID());
        model.setDescricao("Empresa");
        model.setViman(Viman.UFX);
        model.setCodigoEmpresa("001");
        model.setMunicipioId(UUID.randomUUID());
        model.setPossuiEstoqueInterno(true);
        model.setPossuiEstoqueSegregado(true);
        model.setPossuiVp(true);
        model.setCriadoPor(UUID.randomUUID());
        return model;
    }

    public static EmpresaResponseDTO empresaResponseDTO() {

        var model = empresaModel();
        return new EmpresaResponseDTO(model.getId(), model.getDescricao(),
                new MunicipioResponseDTO(model.getMunicipioId(), "Cidade", "123", "SP"),
                model.getViman(), model.getCodigoEmpresa(), model.isPossuiEstoqueInterno(),
                model.isPossuiEstoqueSegregado(), model.isPossuiVp());
    }

    public static EstoqueSegregadoParametroModel estoqueSegregadoModel() {

        var model = new EstoqueSegregadoParametroModel();
        model.setId(UUID.randomUUID());
        model.setIdEmpresa(UUID.randomUUID());
        model.setCodSegregado(10);
        model.setCodIBGE("123");
        model.setCriadoPor(UUID.randomUUID());
        return model;
    }

    public static EstoqueSegregadoResponseDTO estoqueSegregadoResponseDTO() {

        var model = estoqueSegregadoModel();
        return new EstoqueSegregadoResponseDTO(
                model.getId(),
                "Empresa",
                model.getCodSegregado(),
                "Cidade",
                "SP",
                "CD"
        );
    }

    public static MunicipioModel municipioModel() {

        var model = new MunicipioModel();
        model.setId(UUID.randomUUID());
        model.setDescricao("Cidade");
        model.setCodIbge("123");
        model.setEstado("SP");
        return model;
    }

    public static MunicipioResponseDTO municipioResponseDTO() {

        var model = municipioModel();
        return new MunicipioResponseDTO(model.getId(), model.getDescricao(), model.getCodIbge(), model.getEstado());
    }

    public static ForecastAgrupadoResponseDTO forecastAgrupadoResponseDTO() {

        return new ForecastAgrupadoResponseDTO(
                "CD",
                "Empresa",
                "SP",
                "Cidade",
                "ANVISA",
                "Marca",
                "P1",
                "Produto",
                1L,
                2L,
                3L,
                4L,
                5L,
                6L,
                7L,
                8L,
                9L,
                10L
        );
    }

    public static PasswordResetCodeModel passwordResetCodeModel() {

        var model = new PasswordResetCodeModel();
        model.setId(UUID.randomUUID());
        model.setEmail("teste@medic.com");
        model.setCodigo("hash-code");
        model.setExpiraEm(Instant.now().plusSeconds(60));
        model.setUsado(false);
        return model;
    }
}
