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
@Table(name = "ANAG_EVENTO", uniqueConstraints = @UniqueConstraint(name = "uq_anag_evento_nome_tipo", columnNames = { "NOME_EVENTO", "TIPO_EVENTO" }))
public class AnagEvento {

    @Id
    @Column(name = "ID", nullable = false)
    private Short id;

    @Column(name = "NOME_EVENTO")
    private String nomeEvento;

    @Column(name = "TIPO_EVENTO")
    private String tipoEvento;
}

