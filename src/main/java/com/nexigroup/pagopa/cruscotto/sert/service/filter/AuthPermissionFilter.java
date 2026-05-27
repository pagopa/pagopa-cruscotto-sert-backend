package com.nexigroup.pagopa.cruscotto.sert.service.filter;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Size;

public class AuthPermissionFilter implements Serializable {

	@Serial
	private static final long serialVersionUID = -5568783371639105242L;

	@Size(max = 100)
    private String nome;

    @Size(max = 50)
    private String modulo;
    

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("nome", nome).append("modulo", modulo).toString();
    }
}
