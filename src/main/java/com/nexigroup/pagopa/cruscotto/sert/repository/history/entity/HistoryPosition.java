package com.nexigroup.pagopa.cruscotto.sert.repository.history.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "position")
public class HistoryPosition {

    @Id
    private Integer id;

    protected HistoryPosition() {}
}
