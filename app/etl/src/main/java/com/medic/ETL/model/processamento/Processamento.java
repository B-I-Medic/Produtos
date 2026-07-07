package com.medic.ETL.model.processamento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "processamento")
public class Processamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant iniciadoEm;

    @Column
    private Instant concluidoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessamentoStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessamentoEntidade entidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessamentoDisparo tipoDisparo;
}
