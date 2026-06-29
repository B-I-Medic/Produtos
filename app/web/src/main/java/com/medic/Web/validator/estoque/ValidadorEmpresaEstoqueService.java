package com.medic.Web.validator.estoque;

import com.medic.Web.model.empresa.EmpresaModel;
import com.medic.Web.model.empresa.EmpresaMunicipioModel;
import com.medic.Web.repository.empresa.EmpresaMunicipioRepository;
import com.medic.Web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ValidadorEmpresaEstoqueService {

    private final EmpresaMunicipioRepository empresaMunicipioRepository;
    private final EmpresaRepository empresaRepository;

    public ValidadorEmpresaEstoqueService(EmpresaMunicipioRepository empresaMunicipioRepository,
                                          EmpresaRepository empresaRepository) {
        this.empresaMunicipioRepository = empresaMunicipioRepository;
        this.empresaRepository = empresaRepository;
    }

    public Mono<EmpresaMunicipioModel> validarEstoqueInterno(UUID idEmpresa,
                                                             UUID idEmpresaMunicipio) {

        return empresaMunicipioRepository.findById(idEmpresaMunicipio)
                .filter(empresaMunicipio -> empresaMunicipio.getIdEmpresa().equals(idEmpresa))
                .flatMap(empresaMunicipio -> empresaRepository.findById(empresaMunicipio.getIdEmpresa())
                        .filter(EmpresaModel::isPossuiEstoqueInterno)
                        .thenReturn(empresaMunicipio));
    }

    public Mono<EmpresaMunicipioModel> validarEstoqueSegregado(UUID idEmpresa,
                                                               UUID idEmpresaMunicipio) {

        return empresaMunicipioRepository.findById(idEmpresaMunicipio)
                .filter(empresaMunicipio -> empresaMunicipio.getIdEmpresa().equals(idEmpresa))
                .flatMap(empresaMunicipio -> empresaRepository.findById(empresaMunicipio.getIdEmpresa())
                        .filter(EmpresaModel::isPossuiEstoqueSegregado)
                        .thenReturn(empresaMunicipio));
    }

    public Mono<EmpresaMunicipioModel> validarValePermanente(UUID idEmpresa,
                                                             UUID idEmpresaMunicipio) {

        return empresaMunicipioRepository.findById(idEmpresaMunicipio)
                .filter(empresaMunicipio -> empresaMunicipio.getIdEmpresa().equals(idEmpresa))
                .flatMap(empresaMunicipio -> empresaRepository.findById(empresaMunicipio.getIdEmpresa())
                        .filter(EmpresaModel::isPossuiVp)
                        .thenReturn(empresaMunicipio));
    }
}
