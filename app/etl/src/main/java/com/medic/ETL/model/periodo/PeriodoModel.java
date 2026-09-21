package com.medic.ETL.model.periodo;

import com.medic.ETL.model.usuario.UsuarioModel;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "periodo")
public class PeriodoModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, updatable = false, insertable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_periodo", updatable = false, insertable = false)
    private PeriodoTipo tipoPeriodo;

    @Column(name = "quantidade", updatable = false, insertable = false)
    private Integer quantidade;

    @Column(nullable = true, updatable = false, insertable = false)
    private LocalDate dataInicial;

    @Column(nullable = true, updatable = false, insertable = false)
    private LocalDate dataFinal;

    @Column(nullable = true, updatable = false, insertable = false)
    private String dataInicialViman;

    @Column(nullable = true, updatable = false, insertable = false)
    private String dataFinalViman;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por", nullable = false, updatable = false, insertable = false)
    private UsuarioModel atualizadoPor;
}
