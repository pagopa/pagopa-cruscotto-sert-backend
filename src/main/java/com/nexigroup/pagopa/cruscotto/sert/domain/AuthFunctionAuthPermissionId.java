package com.nexigroup.pagopa.cruscotto.sert.domain;

import java.io.Serializable;

public class AuthFunctionAuthPermissionId implements Serializable {

    private Long funzione;

    private Long permesso;

    public Long getFunzione() {
        return funzione;
    }

    public void setFunzione(Long funzione) {
        this.funzione = funzione;
    }

    public Long getPermesso() {
        return permesso;
    }

    public void setPermesso(Long permesso) {
        this.permesso = permesso;
    }
}
