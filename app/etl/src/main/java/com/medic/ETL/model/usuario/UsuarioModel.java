package com.medic.ETL.model.usuario;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "usuario")
public class UsuarioModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false, insertable = false)
    private String nome;

    @Column(unique = true, nullable = false, updatable = false, insertable = false)
    private String email;

    @Column(nullable = false, updatable = false, insertable = false)
    private String senha;

    @Column(nullable = false, updatable = false, insertable = false)
    private Role ROLE;

    @Column(nullable = false, updatable = false, insertable = false)
    private boolean ativo;

    @Column(nullable = false, updatable = false, insertable = false)
    private boolean primeiroAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false, insertable = false)
    private UsuarioModel criadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false, insertable = false)
    private UsuarioModel atualizadoPor;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant atualizadoEm;
}
