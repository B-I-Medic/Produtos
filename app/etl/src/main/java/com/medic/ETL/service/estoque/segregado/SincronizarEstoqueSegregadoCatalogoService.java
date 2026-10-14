
package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import com.medic.ETL.model.empresa.EmpresaModel;
import com.medic.ETL.repository.empresa.EmpresaRepository;
import com.medic.ETL.repository.catalogo.EstoqueSegregadoCatalogoOrigemRepository;
import com.medic.ETL.repository.catalogo.EstoqueSegregadoCatalogoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class SincronizarEstoqueSegregadoCatalogoService {

    private final EmpresaRepository empresaRepository;
    private final EstoqueSegregadoCatalogoOrigemRepository origemRepository;
    private final EstoqueSegregadoCatalogoRepository catalogoRepository;

    public SincronizarEstoqueSegregadoCatalogoService(EmpresaRepository empresaRepository,
                                                       EstoqueSegregadoCatalogoOrigemRepository origemRepository,
                                                       EstoqueSegregadoCatalogoRepository catalogoRepository) {
        this.empresaRepository = empresaRepository;
        this.origemRepository = origemRepository;
        this.catalogoRepository = catalogoRepository;
    }

    public void sincronizar() {

        List<EmpresaModel> empresas = empresaRepository.findAllByPossuiEstoqueSegregadoIsTrue();
        Map<UUID, List<EstoqueSegregadoCatalogoItemDTO>> snapshot = new LinkedHashMap<>();

        for (EmpresaModel empresa : empresas) {
            List<EstoqueSegregadoCatalogoItemDTO> itens = consultarOrigem(empresa);
            snapshot.put(empresa.getId(), new ArrayList<>(itens));
        }

        catalogoRepository.reconciliar(snapshot);
    }

    private List<EstoqueSegregadoCatalogoItemDTO> consultarOrigem(EmpresaModel empresa) {

        String viman = empresa.getViman() == null ? "" : empresa.getViman().trim().toUpperCase(Locale.ROOT);

        return switch (viman) {
            case "UFX" -> origemRepository.consultarUfx(empresa);
            case "S00" -> origemRepository.consultarS00(empresa);
            default -> throw new IllegalStateException(
                    "Viman sem consulta de estoque segregado: " + empresa.getViman()
            );
        };
    }
}
