package com.medic.Web.service.anvisa;

import com.medic.Web.dto.anvisa.AnvisaEmpresaComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaModeloDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoLocalConsulta;
import com.medic.Web.repository.anvisa.AnvisaRepositoryCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaAnvisaServiceTest {

    @Mock
    private AnvisaRepositoryCustom repository;

    @Test
    void shouldConsultTheOfficialProductAndItsModels() {

        var service = new ConsultaAnvisaService(repository);
        AnvisaProdutoDTO product = new AnvisaProdutoDTO(
                "123", "PROC", "Tecnico", "II", "Comercial", "CNPJ", "Detentor",
                "Fabricante", "Brasil", "2026", "2030");
        AnvisaModeloDTO model = new AnvisaModeloDTO(
                "123", "Tecnico", "II", "Comercial", "Detentor", "Fabricante", "Brasil",
                "Modelo 1", "2026", "2030");
        when(repository.findProduto("123")).thenReturn(Mono.just(product));
        when(repository.findModelos("123")).thenReturn(Flux.just(model));

        StepVerifier.create(service.consultar(" 123 "))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(product, response.produto());
                    org.junit.jupiter.api.Assertions.assertEquals(List.of(model), response.modelos());
                })
                .verifyComplete();
    }

    @Test
    void shouldGroupCompaniesAndMergeProductsFromEveryCompanyCode() {

        var service = new ConsultaAnvisaService(repository);

        when(repository.findEmpresas()).thenReturn(Flux.just(
                new AnvisaRepositoryCustom.EmpresaConsulta("Alpine", "S00", "02"),
                new AnvisaRepositoryCustom.EmpresaConsulta("Alpine", "UFX", "01"),
                new AnvisaRepositoryCustom.EmpresaConsulta("Avant", "S00", "03"),
                new AnvisaRepositoryCustom.EmpresaConsulta("Alpine", "S00", "01")
        ));
        when(repository.findProdutosLocais("81420890023")).thenReturn(Flux.just(
                new AnvisaProdutoLocalConsulta("S00", "01", "P2", "Produto B"),
                new AnvisaProdutoLocalConsulta("S00", "02", "P1", "Produto A"),
                new AnvisaProdutoLocalConsulta("S00", "01, 02", "P1", "Produto A"),
                new AnvisaProdutoLocalConsulta("S00", "03", "A1", "Produto Avant"),
                new AnvisaProdutoLocalConsulta("UFX", "01", "U1", "Produto UFX")
        ));

        StepVerifier.create(service.comparar(" 81420890023 "))
                .expectNext(
                        new AnvisaEmpresaComparacaoDTO("Alpine", "S00", List.of(
                                new AnvisaProdutoComparacaoDTO("P1", "Produto A"),
                                new AnvisaProdutoComparacaoDTO("P2", "Produto B")
                        )),
                        new AnvisaEmpresaComparacaoDTO("Avant", "S00", List.of(
                                new AnvisaProdutoComparacaoDTO("A1", "Produto Avant")
                        )),
                        new AnvisaEmpresaComparacaoDTO("Alpine", "UFX", List.of(
                                new AnvisaProdutoComparacaoDTO("U1", "Produto UFX")
                        ))
                )
                .verifyComplete();

        verify(repository).findProdutosLocais("81420890023");
        verify(repository, never()).findProduto(anyString());
        verify(repository, never()).findModelos(anyString());
    }

    @Test
    void shouldKeepProductsWithSameCodeAndDifferentDescriptions() {

        var service = new ConsultaAnvisaService(repository);

        when(repository.findEmpresas()).thenReturn(Flux.just(
                new AnvisaRepositoryCustom.EmpresaConsulta("Alpine", "S00", "01")
        ));
        when(repository.findProdutosLocais("81420890023")).thenReturn(Flux.just(
                new AnvisaProdutoLocalConsulta("S00", "01", "P1", "Descricao A"),
                new AnvisaProdutoLocalConsulta("S00", "01", "P1", "Descricao B")
        ));

        StepVerifier.create(service.comparar("81420890023"))
                .expectNext(new AnvisaEmpresaComparacaoDTO("Alpine", "S00", List.of(
                        new AnvisaProdutoComparacaoDTO("P1", "Descricao A"),
                        new AnvisaProdutoComparacaoDTO("P1", "Descricao B")
                )))
                .verifyComplete();
    }

    @Test
    void shouldExcludeCompaniesWithoutProductsAndMatchCompleteCompanyCodeTokens() {

        var service = new ConsultaAnvisaService(repository);

        when(repository.findEmpresas()).thenReturn(Flux.just(
                new AnvisaRepositoryCustom.EmpresaConsulta("Alpine", "S00", "1"),
                new AnvisaRepositoryCustom.EmpresaConsulta("Avant", "S00", "10"),
                new AnvisaRepositoryCustom.EmpresaConsulta("Sem produto", "UFX", "99")
        ));
        when(repository.findProdutosLocais("81420890023")).thenReturn(Flux.just(
                new AnvisaProdutoLocalConsulta("S00", "10", "P1", "Produto Avant")
        ));

        StepVerifier.create(service.comparar("81420890023"))
                .expectNext(new AnvisaEmpresaComparacaoDTO("Avant", "S00", List.of(
                        new AnvisaProdutoComparacaoDTO("P1", "Produto Avant")
                )))
                .verifyComplete();
    }
}
