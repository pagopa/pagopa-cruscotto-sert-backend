package com.nexigroup.pagopa.cruscotto.sert.service.filter;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Size;

public class AuthGroupFilter implements Serializable {

	@Serial
	private static final long serialVersionUID = -3113933401532441242L;

	@Size(max = 50)
    private String nome;

    @Size(max = 200)
    private String descrizione;
    

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("nome", nome).append("descrizione", descrizione).toString();
    }
}
