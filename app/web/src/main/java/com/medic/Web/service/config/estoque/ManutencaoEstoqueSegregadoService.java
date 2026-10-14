package com.medic.Web.service.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoCatalogoUpdateDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.Web.model.config.estoque.EstoqueSegregadoCatalogoModel;
import com.medic.Web.repository.cd.MunicipioRepository;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class ManutencaoEstoqueSegregadoService {

    private final MunicipioRepository municipioRepository;
    private final EstoqueSegregadoRepository repository;
    private final ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService;

    public ManutencaoEstoqueSegregadoService(MunicipioRepository municipioRepository,
                                             EstoqueSegregadoRepository repository,
                                             ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService) {
        this.municipioRepository = municipioRepository;
        this.repository = repository;
        this.validadorEmpresaEstoqueService = validadorEmpresaEstoqueService;
    }

    @Transactional
    public Mono<Void> update(UUID estoqueSegregadoId,
                             EstoqueSegregadoCatalogoUpdateDTO dto,
                             UUID userId) {

        return repository.findById(estoqueSegregadoId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Estoque segregado nao encontrado")))
                .flatMap(registro -> {
                    if (Boolean.TRUE.equals(dto.ativo())) {
                        return validadorEmpresaEstoqueService.validarEmpresa(registro.getIdEmpresa())
                                .then(atualizarRegistro(registro, dto, userId));
                    }
                    return atualizarRegistro(registro, dto, userId);
                })
                .flatMap(repository::save)
                .then();
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado(EstoqueSegregadoFilterDTO filter) {

        return repository.getAllAndFilter(filter);
    }

    private Mono<EstoqueSegregadoCatalogoModel> atualizarRegistro(EstoqueSegregadoCatalogoModel registro,
                                                                  EstoqueSegregadoCatalogoUpdateDTO dto,
                                                                  UUID userId) {

        if (dto.ativo() == null) {
            return Mono.error(new IllegalArgumentException("O estado de ativacao e obrigatorio"));
        }

        Mono<EstoqueSegregadoCatalogoModel> registroComMunicipio;

        if (dto.ativo() && dto.idMunicipio() != null) {
            registroComMunicipio = municipioRepository.findById(dto.idMunicipio())
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Municipio nao encontrado")))
                    .map(municipio -> {
                        registro.setCodIBGE(municipio.getCodIbge());
                        return registro;
                    });
        } else {
            registroComMunicipio = Mono.just(registro);
        }

        return registroComMunicipio.flatMap(estoqueSegregado -> {
            if (dto.ativo() && estoqueSegregado.getCodIBGE() == null) {
                return Mono.error(new IllegalArgumentException("Informe o municipio para ativar o estoque segregado"));
            }

            estoqueSegregado.setAtivo(dto.ativo());
            estoqueSegregado.setAtualizadoPor(userId);
            estoqueSegregado.setAtualizadoEm(Instant.now());
            return Mono.just(estoqueSegregado);
        });
    }
}
