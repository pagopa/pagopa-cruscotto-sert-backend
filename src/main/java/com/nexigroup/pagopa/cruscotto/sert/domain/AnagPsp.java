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
@Table(name = "ANAG_PSP", uniqueConstraints = @UniqueConstraint(name = "uq_anag_psp_codice", columnNames = "CODICE"))
public class AnagPsp {

    @Id
    @Column(name = "ID", nullable = false)
    private Short id;

    @Column(name = "CODICE")
    private String codice;

    @Column(name = "DESCRIPTION")
    private String description;
}

