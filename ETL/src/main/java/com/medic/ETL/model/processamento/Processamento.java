package com.medic.ETL.model.processamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "processamento", schema = "public")
public class Processamento {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "iniciado_em", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime iniciadoEm;

    @Column(name = "concluido_em", columnDefinition = "timestamp with time zone")
    private OffsetDateTime concluidoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessamentoStatus status;
}
