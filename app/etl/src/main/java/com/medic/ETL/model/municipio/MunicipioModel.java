package com.medic.ETL.model.municipio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "municipio")
public class MunicipioModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false, insertable = false)
    private String descricao;

    @Column(name = "cod_ibge", nullable = false, updatable = false, insertable = false)
    private String codIBGE;

    @Column(nullable = false, updatable = false, insertable = false)
    private String estado;

}