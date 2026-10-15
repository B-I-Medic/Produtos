package com.medic.web.validator.estoque;

import com.medic.web.repository.empresa.EmpresaRepository;
import com.medic.web.support.TestDataFactory;
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
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private ValidadorEmpresaEstoqueService service;

    @Test
    void shouldValidateCompanyWithSegregatedStock() {

        var empresa = TestDataFactory.empresaModel();
        when(empresaRepository.existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(empresa.getId()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.validarEmpresa(empresa.getId())).verifyComplete();
    }

    @Test
    void shouldRejectCompanyWithoutSegregatedStock() {

        var empresa = TestDataFactory.empresaModel();
        when(empresaRepository.existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(empresa.getId()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(service.validarEmpresa(empresa.getId()))
                .expectErrorMessage("Empresa não possui estoque segregado ou não existe")
                .verify();
    }

}
