package com.medic.Web.validator.estoque;

import com.medic.Web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ValidadorEmpresaEstoqueService {

    private final EmpresaRepository empresaMunicipioRepository;

    public ValidadorEmpresaEstoqueService(EmpresaRepository empresaMunicipioRepository) {
        this.empresaMunicipioRepository = empresaMunicipioRepository;
    }

    public Mono<Void> validarEmpresa(UUID idEmpresa) {

        return empresaMunicipioRepository.findByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(idEmpresa)
                .filter(Boolean.TRUE::equals)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Empresa não possui estoque segregado ou não existe")))
                .then();
    }
}
