package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "POSITION")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_position")
    @SequenceGenerator(name = "seq_position", sequenceName = "SQ_POSITION", allocationSize = 1)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DATE_EVENT")
    private LocalDate dateEvent;

    @Column(name = "INSERTED_TIMESTAMP")
    private LocalDateTime insertedTimestamp;

    @Column(name = "NAV")
    private String nav;

    @Column(name = "PA_EMITTENTE")
    private String paEmittente;

    @Column(name = "LAST_EVENT")
    private LocalDateTime lastEvent;

    @Column(name = "DATE_EVENTS", columnDefinition = "jsonb")
    private String dateEvents;
}
