package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaModeloDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoLocalConsulta;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class AnvisaRepositoryCustomImpl implements AnvisaRepositoryCustom {

    private final DatabaseClient databaseClient;

    public AnvisaRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<AnvisaProdutoDTO> findProduto(String codAnvisa) {

        return databaseClient.sql("""
                        select
                            p.numero_registro_cadastro,
                            p.numero_processo,
                            p.nome_tecnico,
                            p.classe_risco,
                            p.nome_comercial,
                            p.cnpj_detentor_registro_cadastro,
                            p.detentor_registro_cadastro,
                            p.nome_fabricante,
                            p.nome_pais_fabric,
                            p.dt_pub_registro_cadastro,
                            p.validade_registro_cadastro
                        from anvisa_produto p
                        join anvisa_carga c on c.id = p.carga_id and c.ativa = true
                        where p.numero_registro_cadastro = :codAnvisa
                        """)
                .bind("codAnvisa", codAnvisa)
                .map((row, metadata) -> new AnvisaProdutoDTO(
                        row.get("numero_registro_cadastro", String.class),
                        row.get("numero_processo", String.class),
                        row.get("nome_tecnico", String.class),
                        row.get("classe_risco", String.class),
                        row.get("nome_comercial", String.class),
                        row.get("cnpj_detentor_registro_cadastro", String.class),
                        row.get("detentor_registro_cadastro", String.class),
                        row.get("nome_fabricante", String.class),
                        row.get("nome_pais_fabric", String.class),
                        row.get("dt_pub_registro_cadastro", String.class),
                        row.get("validade_registro_cadastro", String.class)
                ))
                .all()
                .next();
    }

    @Override
    public Flux<AnvisaModeloDTO> findModelos(String codAnvisa) {

        return databaseClient.sql("""
                        select
                            m.numero_registro_cadastro,
                            m.nome_tecnico,
                            m.classe_risco,
                            m.nome_comercial,
                            m.detentor_registro_cadastro,
                            m.nome_fabricante,
                            m.nome_pais_fabric,
                            m.ds_modelo_produto_medico,
                            m.dt_pub_registro_cadastro,
                            m.validade_registro_cadastro
                        from anvisa_modelo m
                        join anvisa_carga c on c.id = m.carga_id and c.ativa = true
                        where m.numero_registro_cadastro = :codAnvisa
                        order by m.modelo_ordem
                        """)
                .bind("codAnvisa", codAnvisa)
                .map((row, metadata) -> new AnvisaModeloDTO(
                        row.get("numero_registro_cadastro", String.class),
                        row.get("nome_tecnico", String.class),
                        row.get("classe_risco", String.class),
                        row.get("nome_comercial", String.class),
                        row.get("detentor_registro_cadastro", String.class),
                        row.get("nome_fabricante", String.class),
                        row.get("nome_pais_fabric", String.class),
                        row.get("ds_modelo_produto_medico", String.class),
                        row.get("dt_pub_registro_cadastro", String.class),
                        row.get("validade_registro_cadastro", String.class)
                ))
                .all();
    }

    @Override
    public Flux<EmpresaConsulta> findEmpresas() {

        return databaseClient.sql("""
                        select e.descricao, e.viman, e.codigo_empresa
                        from empresa e
                        order by e.viman, e.descricao, e.codigo_empresa
                        """)
                .map((row, metadata) -> new EmpresaConsulta(
                row.get("descricao", String.class),
                row.get("viman", String.class),
                row.get("codigo_empresa", String.class)
                ))
                .all();
    }

    @Override
    public Flux<AnvisaProdutoLocalConsulta> findProdutosLocais(String codAnvisa) {

        return databaseClient.sql("""
                        select viman, cod_empresa, cod_produto, descricao
                        from produto
                        where anvisa = :codAnvisa
                        order by viman, cod_empresa, cod_produto, descricao
                        """)
                .bind("codAnvisa", codAnvisa)
                .map((row, metadata) -> new AnvisaProdutoLocalConsulta(
                        row.get("viman", String.class),
                        row.get("cod_empresa", String.class),
                        row.get("cod_produto", String.class),
                        row.get("descricao", String.class)
                ))
                .all();
    }
}
