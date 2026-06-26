package com.medic.Produtos.validator.estoque;

import com.medic.Produtos.model.empresa.EmpresaModel;
import com.medic.Produtos.model.empresa.EmpresaMunicipioModel;
import com.medic.Produtos.repository.empresa.EmpresaMunicipioRepository;
import com.medic.Produtos.repository.empresa.EmpresaRepository;
import com.medic.Produtos.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorEmpresaEstoqueServiceTest {

    @Mock
    private EmpresaMunicipioRepository empresaMunicipioRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private ValidadorEmpresaEstoqueService service;

    @Test
    void shouldValidateEstoqueInternoSegregadoAndVp() {

        EmpresaMunicipioModel empresaMunicipio = TestDataFactory.empresaMunicipioModel();
        EmpresaModel empresa = TestDataFactory.empresaModel();
        empresaMunicipio.setIdEmpresa(empresa.getId());

        when(empresaMunicipioRepository.findById(empresaMunicipio.getId())).thenReturn(Mono.just(empresaMunicipio));
        when(empresaRepository.findById(empresa.getId())).thenReturn(Mono.just(empresa));

        StepVerifier.create(service.validarEstoqueInterno(empresa.getId(), empresaMunicipio.getId()))
                .expectNext(empresaMunicipio)
                .verifyComplete();

        StepVerifier.create(service.validarEstoqueSegregado(empresa.getId(), empresaMunicipio.getId()))
                .expectNext(empresaMunicipio)
                .verifyComplete();

        StepVerifier.create(service.validarValePermanente(empresa.getId(), empresaMunicipio.getId()))
                .expectNext(empresaMunicipio)
                .verifyComplete();
    }
}
