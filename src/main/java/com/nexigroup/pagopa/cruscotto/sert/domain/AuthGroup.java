package com.nexigroup.pagopa.cruscotto.sert.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A AuthGroup.
 */
@Entity
@Table(name = "AUTH_GROUP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CO_ID")
    @SequenceGenerator(name = "SQCRUSC8_AUTHGROU", sequenceName = "SQCRUSC8_AUTHGROU", allocationSize = 1)
    @GeneratedValue(generator = "SQCRUSC8_AUTHGROU", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Size(min = 1, max = 50)
    @Column(name = "TE_NOME", length = 50)
    private String nome;

    @Size(min = 1, max = 100)
    @Column(name = "TE_OBJECTID", length = 100)
    private String objectId;


    @Size(min = 1, max = 200)
    @Column(name = "TE_DESCRIZIONE", length = 200)
    private String descrizione;

    @NotNull
    @Column(name = "FL_LIVELLO_VISIBILITA")
    private Integer livelloVisibilita;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(
        name = "AUTH_GROUP_AUTH_FUNC",
        joinColumns = @JoinColumn(name = "CO_AUTH_GROUP_ID", referencedColumnName = "CO_ID"),
        inverseJoinColumns = @JoinColumn(name = "CO_AUTH_FUNCTION_ID", referencedColumnName = "CO_ID")
    )
    private Set<AuthFunction> authFunctions = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<AuthUser> authUsers = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public AuthGroup nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public AuthGroup descrizione(String descrizione) {
        this.descrizione = descrizione;
        return this;
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

    public Set<AuthFunction> getAuthFunctions() {
        return authFunctions;
    }

    public AuthGroup authFunctions(Set<AuthFunction> authFunctions) {
        this.authFunctions = authFunctions;
        return this;
    }

    public AuthGroup addAuthFunction(AuthFunction authFunction) {
        this.authFunctions.add(authFunction);
        authFunction.getAuthGroups().add(this);
        return this;
    }

    public AuthGroup removeAuthFunction(AuthFunction authFunction) {
        this.authFunctions.remove(authFunction);
        authFunction.getAuthGroups().remove(this);
        return this;
    }

    public void setAuthFunctions(Set<AuthFunction> authFunctions) {
        this.authFunctions = authFunctions;
    }

    public Set<AuthUser> getAuthUsers() {
        return authUsers;
    }

    public void setAuthUsers(Set<AuthUser> authUsers) {
        this.authUsers = authUsers;
    }

    public AuthGroup addAuthUser(AuthUser authUser) {
        this.authUsers.add(authUser);
        authUser.setGroup(this);
        return this;
    }

    public AuthGroup removeAuthUser(AuthUser authUser) {
        this.authUsers.remove(authUser);
        authUser.setGroup(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthGroup)) {
            return false;
        }
        return id != null && id.equals(((AuthGroup) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AuthGroup{" +
            "id=" + id +
            ", nome='" + nome + '\'' +
            ", objectId='" + objectId + '\'' +
            ", descrizione='" + descrizione + '\'' +
            ", livelloVisibilita=" + livelloVisibilita +
            ", authFunctions=" + authFunctions +
            ", authUsers=" + authUsers +
            '}';
    }
}
