package com.medic.ETL.model.empresa;

import com.medic.ETL.model.usuario.UsuarioModel;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "empresa")
public class EmpresaModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false, insertable = false)
    private String descricao;

    @Column(nullable = false, updatable = false, insertable = false)
    private String viman;

    @Column(nullable = false, updatable = false, insertable = false)
    private String codigoEmpresa;

    @Column(nullable = false, updatable = false, insertable = false)
    private boolean possuiEstoqueInterno;

    @Column(nullable = false, updatable = false, insertable = false)
    private boolean possuiEstoqueSegregado;

    @Column(nullable = false, updatable = false, insertable = false)
    private boolean possuiVp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por", nullable = false, updatable = false, insertable = false)
    private UsuarioModel criadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por", nullable = false, updatable = false, insertable = false)
    private UsuarioModel atualizadoPor;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant atualizadoEm;
}
