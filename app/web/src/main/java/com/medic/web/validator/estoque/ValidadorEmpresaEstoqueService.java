package com.medic.web.validator.estoque;

import com.medic.web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ValidadorEmpresaEstoqueService {

    private final EmpresaRepository empresaRepository;

    public ValidadorEmpresaEstoqueService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Mono<Void> validarEmpresa(UUID idEmpresa) {

        return empresaRepository.existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(idEmpresa)
                .filter(Boolean.TRUE::equals)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Empresa não possui estoque segregado ou não existe")))
                .then();
    }
}
