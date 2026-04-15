package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@IdClass(AuthFunctionAuthPermissionId.class)
@DynamicUpdate
@DynamicInsert
@Table(name = "AUTH_FUN_AUTH_PERM")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthFunctionAuthPermission implements Serializable {

    private static final long serialVersionUID = 2063225717101367016L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_AUTH_FUNCTION_ID", nullable = false)
    private AuthFunction funzione;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_AUTH_PERMISSION_ID", nullable = false)
    private AuthPermission permesso;

    public AuthFunction getFunzione() {
        return funzione;
    }

    public void setFunzione(AuthFunction funzione) {
        this.funzione = funzione;
    }

    public AuthPermission getPermesso() {
        return permesso;
    }

    public void setPermesso(AuthPermission permesso) {
        this.permesso = permesso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthFunctionAuthPermission that = (AuthFunctionAuthPermission) o;
        return Objects.equals(funzione, that.funzione) && Objects.equals(permesso, that.permesso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(funzione, permesso);
    }
}
