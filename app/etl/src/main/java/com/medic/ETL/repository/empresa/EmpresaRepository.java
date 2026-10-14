package com.medic.ETL.repository.empresa;

import com.medic.ETL.dto.empresa.EmpresaEstoqueInternoDTO;
import com.medic.ETL.dto.empresa.EmpresaValePermanenteDTO;
import com.medic.ETL.model.empresa.EmpresaModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<EmpresaModel, UUID> {

    List<EmpresaModel> findAllByVimanAndPossuiEstoqueInternoIsTrue(String viman);

    List<EmpresaModel> findAllByPossuiEstoqueSegregadoIsTrue();

    @Query("""
            select new com.medic.ETL.dto.empresa.EmpresaEstoqueInternoDTO(
                e.viman,
                e.codigoEmpresa,
                m.codIBGE
            )
            from EmpresaModel e
            join e.municipio m
            where e.viman = :viman
              and e.possuiEstoqueInterno = true
            order by e.codigoEmpresa, m.codIBGE
            """)
    List<EmpresaEstoqueInternoDTO> findAllEstoqueInternoByViman(@Param("viman") String viman);

    @Query("""
            select new com.medic.ETL.dto.empresa.EmpresaValePermanenteDTO(
                e.viman,
                e.codigoEmpresa
            )
            from EmpresaModel e
            where e.viman = :viman
              and e.possuiVp = true
            order by e.viman, e.codigoEmpresa
            """)
    List<EmpresaValePermanenteDTO> findAllValePermanenteByViman(@Param("viman") String viman);

}
