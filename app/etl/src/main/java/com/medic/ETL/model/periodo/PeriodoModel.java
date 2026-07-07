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

    @Column(nullable = false, updatable = false, insertable = false)
    private LocalDate dataInicial;

    @Column(nullable = false, updatable = false, insertable = false)
    private LocalDate dataFinal;

    @Column(nullable = false, updatable = false, insertable = false)
    private String dataInicialViman;

    @Column(nullable = false, updatable = false, insertable = false)
    private String dataFinalViman;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por", nullable = false, updatable = false, insertable = false)
    private UsuarioModel atualizadoPor;
}
