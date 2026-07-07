package com.medic.ETL.service.produto;

import com.medic.ETL.dto.consulta.ProdutoConsultaDTO;
import org.springframework.stereotype.Service;

@Service
public class PrepararConsultaProdutoService {

    private static final String CONSULTA_UFX = """
            SELECT
                'UFX' AS Viman,
                '01,03,04,05,06,13' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR01 AS PR
            JOIN SYSADM.VETEMA01 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'UFX' AS Viman,
                '07' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR07 AS PR
            JOIN SYSADM.VETEMA07 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU07 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'UFX' AS Viman,
                '08' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR08 AS PR
            JOIN SYSADM.VETEMA08 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU08 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'UFX' AS Viman,
                '11' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR10 AS PR
            JOIN SYSADM.VETEMA10 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU10 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1
            """;

    private static final String CONSULTA_S00 = """
            SELECT
                'S00' AS Viman,
                '01' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR01 AS PR
            JOIN SYSADM.VETEMA01 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '02' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR02 AS PR
            JOIN SYSADM.VETEMA02 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '03' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR03 AS PR
            JOIN SYSADM.VETEMA03 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '04' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR04 AS PR
            JOIN SYSADM.VETEMA04 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '06' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR06 AS PR
            JOIN SYSADM.VETEMA06 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '07' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR07 AS PR
            JOIN SYSADM.VETEMA07 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '08' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR08 AS PR
            JOIN SYSADM.VETEMA08 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '09' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR09 AS PR
            JOIN SYSADM.VETEMA09 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '10' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR10 AS PR
            JOIN SYSADM.VETEMA10 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '11' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR11 AS PR
            JOIN SYSADM.VETEMA11 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1

            UNION ALL

            SELECT
                'S00' AS Viman,
                '12' AS CodEmpresa,
                TRIM(PR.PRCDPR) AS CodProduto,
                TRIM(PR.PRNOME) AS Descricao,
                TRIM(MA.MADESC) AS Marca,
                CASE
                    WHEN PR.PRTPRM = 1 THEN 'Implantes/Descartaveis'
                    WHEN PR.PRTPRM = 2 THEN 'Instrumental/Equipamento'
                    WHEN PR.PRTPRM = 3 THEN 'Imobilizado'
                    ELSE 'Tipo não identificado'
                    END AS Tipo,
                TRIM(PR.PRNRRG) AS Anvisa,
                CASE
                    WHEN PR.PRSITU = 'A' THEN 'Ativo em catálogo'
                    WHEN PR.PRSITU = 'B' THEN 'Ponta de estoque em catálogo'
                    WHEN PR.PRSITU = 'C' THEN 'Fora de linha em catálogo'
                    WHEN PR.PRSITU = 'F' THEN 'Ativo descontinuado'
                    WHEN PR.PRSITU = 'G' THEN 'Ponta de estoque descontinuado'
                    WHEN PR.PRSITU = 'H' THEN 'Fora de linha descontinuado'
                    ELSE 'Situação não identificada'
                END AS Situacao,
                CASE
                    WHEN FU.FUNOME IS NULL THEN 'Não identificado'
                    ELSE TRIM(FU.FUNOME)
                END AS CriadoPor,
                CASE
                    WHEN PR.PRDTCD = 0 THEN NULL
                    ELSE SUBSTRING(cast(PR.PRDTCD as char(8)), 1, 4) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 5, 2) || '-' ||
                        SUBSTRING(cast(PR.PRDTCD as char(8)), 7, 2) || ' 00:00:00'
                END AS CriadoEm
            FROM SYSADM.VETEPR12 AS PR
            JOIN SYSADM.VETEMA12 AS MA
              ON MA.MACODI = PR.PRCDMA
            LEFT JOIN SYSADM.VETEFU01 AS FU
              ON FU.FUCODI = PR.PRFUNC
            WHERE PR.PRTPRM = 1
            """;

    public ProdutoConsultaDTO montarConsultas() {
        return new ProdutoConsultaDTO(CONSULTA_UFX, CONSULTA_S00);
    }
}
