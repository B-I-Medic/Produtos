package com.medic.ETL.model.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "config_schedule")
public class ScheduleModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, insertable = false)
    private ScheduleJob job;

    @Column(updatable = false, insertable = false)
    private String cron;

    @Column(updatable = false, insertable = false)
    private boolean ativo;

    @Column(updatable = false, insertable = false)
    private UUID atualizadoPor;

    @Column(updatable = false, insertable = false)
    private Instant atualizadoEm;
}
