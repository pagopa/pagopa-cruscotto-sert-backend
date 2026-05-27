package com.nexigroup.pagopa.cruscotto.sert.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AuthPermission.
 */
@Entity
@Table(name = "AUTH_PERMISSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CO_ID")
    @SequenceGenerator(name = "SQCRUSC8_AUTHPERM", sequenceName = "SQCRUSC8_AUTHPERM", allocationSize = 1)
    @GeneratedValue(generator = "SQCRUSC8_AUTHPERM", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Size(min = 1, max = 100)
    @Column(name = "TE_NOME", length = 100)
    private String nome;

    @Size(min = 1, max = 50)
    @Column(name = "TE_MODULO", length = 50)
    private String modulo;

    @ManyToMany(mappedBy = "authPermissions")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private Set<AuthFunction> authFunctions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public AuthPermission nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModulo() {
        return modulo;
    }

    public AuthPermission modulo(String modulo) {
        this.modulo = modulo;
        return this;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public Set<AuthFunction> getAuthFunctions() {
        return authFunctions;
    }

    public AuthPermission authFunctions(Set<AuthFunction> authFunctions) {
        this.authFunctions = authFunctions;
        return this;
    }

    public AuthPermission addAuthFunction(AuthFunction authFunction) {
        this.authFunctions.add(authFunction);
        authFunction.getAuthPermissions().add(this);
        return this;
    }

    public AuthPermission removeAuthFunction(AuthFunction authFunction) {
        this.authFunctions.remove(authFunction);
        authFunction.getAuthPermissions().remove(this);
        return this;
    }

    public void setAuthFunctions(Set<AuthFunction> authFunctions) {
        this.authFunctions = authFunctions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthPermission)) {
            return false;
        }
        return id != null && id.equals(((AuthPermission) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AuthPermission{" + "id=" + getId() + ", nome='" + getNome() + "'" + ", modulo='" + getModulo() + "'" + "}";
    }
}
