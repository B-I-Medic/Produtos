package com.medic.Web.service.anvisa;

import com.medic.Web.dto.anvisa.AnvisaEmpresaComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoLocalConsulta;
import com.medic.Web.dto.anvisa.AnvisaResponseDTO;
import com.medic.Web.repository.anvisa.AnvisaRepositoryCustom;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsultaAnvisaService {

    private final AnvisaRepositoryCustom repository;

    public ConsultaAnvisaService(AnvisaRepositoryCustom repository) {
        this.repository = repository;
    }

    public Flux<AnvisaResponseDTO> consultar(String codAnvisa) {

        return repository.findProduto(codAnvisa.trim())
                .flatMapMany(produto -> repository.findModelos(codAnvisa.trim())
                        .collectList()
                        .map(modelos -> new AnvisaResponseDTO(produto, modelos)));
    }

    public Flux<AnvisaEmpresaComparacaoDTO> comparar(String codAnvisa) {

        String codigo = codAnvisa.trim();

        return Mono.zip(
                        repository.findEmpresas().collectList(),
                        repository.findProdutosLocais(codigo).collectList()
                )
                .flatMapMany(data -> Flux.fromIterable(agruparEmpresas(data.getT1(), data.getT2())));
    }

    private List<AnvisaEmpresaComparacaoDTO> agruparEmpresas(List<AnvisaRepositoryCustom.EmpresaConsulta> empresas,
                                                             List<AnvisaProdutoLocalConsulta> produtosLocais) {

        Comparator<AnvisaRepositoryCustom.EmpresaConsulta> ordemEmpresas = Comparator
                .comparing(AnvisaRepositoryCustom.EmpresaConsulta::viman)
                .thenComparing(AnvisaRepositoryCustom.EmpresaConsulta::descricao)
                .thenComparing(AnvisaRepositoryCustom.EmpresaConsulta::codigoEmpresa);

        Map<EmpresaChave, Set<String>> codigosPorEmpresa = new LinkedHashMap<>();

        empresas.stream()
                .sorted(ordemEmpresas)
                .forEach(empresa -> codigosPorEmpresa
                        .computeIfAbsent(
                                new EmpresaChave(empresa.viman(), empresa.descricao()),
                                key -> new LinkedHashSet<>()
                        )
                        .add(normalizarCodigo(empresa.codigoEmpresa())));

        return codigosPorEmpresa.entrySet().stream()
                .map(entry -> new AnvisaEmpresaComparacaoDTO(
                        entry.getKey().descricao(),
                        entry.getKey().viman(),
                        buscarProdutos(entry.getKey(), entry.getValue(), produtosLocais)
                ))
                .filter(empresa -> !empresa.produtos().isEmpty())
                .toList();
    }

    private List<AnvisaProdutoComparacaoDTO> buscarProdutos(EmpresaChave empresa,
                                                            Set<String> codigosEmpresa,
                                                            List<AnvisaProdutoLocalConsulta> produtosLocais) {

        Comparator<AnvisaProdutoComparacaoDTO> ordemProdutos = Comparator
                .comparing(AnvisaProdutoComparacaoDTO::codProduto)
                .thenComparing(AnvisaProdutoComparacaoDTO::descricao);

        return produtosLocais.stream()
                .filter(produto -> equalsIgnoreCase(produto.viman(), empresa.viman()))
                .filter(produto -> pertenceAAlgumCodigo(produto.codEmpresa(), codigosEmpresa))
                .map(produto -> new AnvisaProdutoComparacaoDTO(
                        produto.codProduto(),
                        produto.descricao()
                ))
                .distinct()
                .sorted(ordemProdutos)
                .toList();
    }

    private boolean pertenceAAlgumCodigo(String codigosProduto,
                                         Set<String> codigosEmpresa) {

        String codigosNormalizados = "," + normalizarCodigo(codigosProduto) + ",";

        return codigosEmpresa.stream()
                .anyMatch(codigo -> codigosNormalizados.contains("," + codigo + ","));
    }

    private String normalizarCodigo(String codigo) {

        return codigo.replace(" ", "");
    }

    private boolean equalsIgnoreCase(String first, String second) {

        return first != null && first.equalsIgnoreCase(second);
    }

    private record EmpresaChave(String viman, String descricao) {
    }
}
