package com.nexigroup.pagopa.cruscotto.sert.service.bean;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public class AuthGroupUpdateRequestBean implements Serializable {

    private static final long serialVersionUID = 2252927430223005965L;

    @NotEmpty
    @Digits(integer = 19, fraction = 0)
    private String id;

    @NotEmpty
    @Pattern(regexp = "^[0-9]{0,9}$")
    private String livelloVisibilita;

    public String getId() {
        return id;
    }

    public String getLivelloVisibilita() {
        return livelloVisibilita;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLivelloVisibilita(String livelloVisibilita) {
        this.livelloVisibilita = livelloVisibilita;
    }

    @Override
    public String toString() {
        return "AuthGroupUpdateRequestBean [id=" + id + ", livelloVisibilita=" + livelloVisibilita + "]";
    }
}
