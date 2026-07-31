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
@Table(name = "ANAG_INTERMEDIARIO_PA", schema = "sert_ingestor", uniqueConstraints = @UniqueConstraint(name = "uq_anag_intermediario_pa_codice", columnNames = "CODICE"))
public class AnagIntermediarioPa {

    @Id
    @Column(name = "ID", nullable = false)
    private Short id;

    @Column(name = "CODICE")
    private String codice;

    @Column(name = "DESCRIPTION")
    private String description;
}
