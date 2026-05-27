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
 * A AuthFunction.
 */
@Entity
@Table(name = "AUTH_FUNCTION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CO_ID")
    @SequenceGenerator(name = "SQCRUSC8_AUTHFUNC", sequenceName = "SQCRUSC8_AUTHFUNC", allocationSize = 1)
    @GeneratedValue(generator = "SQCRUSC8_AUTHFUNC", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Size(min = 1, max = 50)
    @Column(name = "TE_NOME", length = 50)
    private String nome;

    @Size(min = 1, max = 50)
    @Column(name = "TE_MODULO", length = 50)
    private String modulo;

    @Size(min = 1, max = 200)
    @Column(name = "TE_DESCRIZIONE", length = 200)
    private String descrizione;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(
        name = "AUTH_FUN_AUTH_PERM",
        joinColumns = @JoinColumn(name = "CO_AUTH_FUNCTION_ID", referencedColumnName = "CO_ID"),
        inverseJoinColumns = @JoinColumn(name = "CO_AUTH_PERMISSION_ID", referencedColumnName = "CO_ID")
    )
    private Set<AuthPermission> authPermissions = new HashSet<>();

    @ManyToMany(mappedBy = "authFunctions")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private Set<AuthGroup> authGroups = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public AuthFunction nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModulo() {
        return modulo;
    }

    public AuthFunction modulo(String modulo) {
        this.modulo = modulo;
        return this;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public AuthFunction descrizione(String descrizione) {
        this.descrizione = descrizione;
        return this;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Set<AuthPermission> getAuthPermissions() {
        return authPermissions;
    }

    public AuthFunction authPermissions(Set<AuthPermission> authPermissions) {
        this.authPermissions = authPermissions;
        return this;
    }

    public AuthFunction addAuthPermission(AuthPermission authPermission) {
        this.authPermissions.add(authPermission);
        authPermission.getAuthFunctions().add(this);
        return this;
    }

    public AuthFunction removeAuthPermission(AuthPermission authPermission) {
        this.authPermissions.remove(authPermission);
        authPermission.getAuthFunctions().remove(this);
        return this;
    }

    public void setAuthPermissions(Set<AuthPermission> authPermissions) {
        this.authPermissions = authPermissions;
    }

    public Set<AuthGroup> getAuthGroups() {
        return authGroups;
    }

    public AuthFunction authGroups(Set<AuthGroup> authGroups) {
        this.authGroups = authGroups;
        return this;
    }

    public AuthFunction addAuthGroup(AuthGroup authGroup) {
        this.authGroups.add(authGroup);
        authGroup.getAuthFunctions().add(this);
        return this;
    }

    public AuthFunction removeAuthGroup(AuthGroup authGroup) {
        this.authGroups.remove(authGroup);
        authGroup.getAuthFunctions().remove(this);
        return this;
    }

    public void setAuthGroups(Set<AuthGroup> authGroups) {
        this.authGroups = authGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthFunction)) {
            return false;
        }
        return id != null && id.equals(((AuthFunction) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return (
            "AuthFunction{" +
            "id=" +
            getId() +
            ", nome='" +
            getNome() +
            "'" +
            ", modulo='" +
            getModulo() +
            "'" +
            ", descrizione='" +
            getDescrizione() +
            "'" +
            "}"
        );
    }
}
