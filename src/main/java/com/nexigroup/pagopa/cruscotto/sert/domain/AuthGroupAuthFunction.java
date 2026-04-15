package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@IdClass(AuthGroupAuthFunctionId.class)
@DynamicUpdate
@DynamicInsert
@Table(name = "AUTH_GROUP_AUTH_FUNC")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthGroupAuthFunction implements Serializable {

    private static final long serialVersionUID = -2881518986455609492L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_AUTH_FUNCTION_ID", nullable = false)
    private AuthFunction funzione;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_AUTH_GROUP_ID", nullable = false)
    private AuthGroup gruppo;

    public AuthFunction getFunzione() {
        return funzione;
    }

    public void setFunzione(AuthFunction funzione) {
        this.funzione = funzione;
    }

    public AuthGroup getGruppo() {
        return gruppo;
    }

    public void setGruppo(AuthGroup gruppo) {
        this.gruppo = gruppo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthGroupAuthFunction that = (AuthGroupAuthFunction) o;
        return Objects.equals(funzione, that.funzione) && Objects.equals(gruppo, that.gruppo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(funzione, gruppo);
    }
}
