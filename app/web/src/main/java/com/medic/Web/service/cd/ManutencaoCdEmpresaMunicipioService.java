package com.medic.Web.service.cd;

import com.medic.Web.dto.cd.CdEmpresaMunicipioFilterDTO;
import com.medic.Web.dto.cd.CdEmpresaMunicipioResponseDTO;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ManutencaoCdEmpresaMunicipioService {

    private final CdEmpresaMunipioRepository repository;

    public ManutencaoCdEmpresaMunicipioService(CdEmpresaMunipioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<CdEmpresaMunicipioResponseDTO> listCdEmpresaMunicipio(UUID cdId,
                                                                      CdEmpresaMunicipioFilterDTO filter) {

        return repository.findByFiltro(cdId, filter);
    }
}
