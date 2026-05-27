package com.nexigroup.pagopa.cruscotto.sert.domain;

import java.io.Serializable;

public class AuthGroupAuthFunctionId implements Serializable {

    private Long gruppo;

    private Long funzione;

    public Long getGruppo() {
        return gruppo;
    }

    public void setGruppo(Long gruppo) {
        this.gruppo = gruppo;
    }

    public Long getFunzione() {
        return funzione;
    }

    public void setFunzione(Long funzione) {
        this.funzione = funzione;
    }
}
