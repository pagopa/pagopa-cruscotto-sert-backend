package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ANAG_PA_EMITTENTE", schema = "sert_ingestor", uniqueConstraints = @UniqueConstraint(name = "anag_pa_emittente_pk", columnNames = "CODICE"))
public class AnagPaEmittente {

    @Id
    @Column(name = "ID", nullable = false)
    private Short id;

    @Column(name = "CODICE", nullable = false)
    private String codice;

    @Column(name = "DESCRIPTION")
    private String description;
}
