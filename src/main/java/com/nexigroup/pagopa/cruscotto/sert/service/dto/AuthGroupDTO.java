package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A DTO for the {@link AuthGroup} entity.
 */
@ToString
@EqualsAndHashCode
public class AuthGroupDTO implements Serializable {

    private static final long serialVersionUID = 7850898416360531158L;

    private Long id;

    @NotEmpty
    @Size(max = 50)
    private String nome;

    @NotEmpty
    @Size(max = 200)
    private String descrizione;

    private Integer livelloVisibilita;

    private Set<AuthFunctionDTO> authFunctions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getLivelloVisibilita() {
        return livelloVisibilita;
    }

    public void setLivelloVisibilita(Integer livelloVisibilita) {
        this.livelloVisibilita = livelloVisibilita;
    }

    public Set<AuthFunctionDTO> getAuthFunctions() {
        return authFunctions;
    }

    public void setAuthFunctions(Set<AuthFunctionDTO> authFunctions) {
        this.authFunctions = authFunctions;
    }
}
