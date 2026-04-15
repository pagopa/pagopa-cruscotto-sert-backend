package com.nexigroup.pagopa.cruscotto.sert.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "AUTH_USER_HISTORY")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthUserHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CO_ID")
    @SequenceGenerator(name = "SQCRUSC8_AUTHUSERHIST", sequenceName = "SQCRUSC8_AUTHUSERHIST", allocationSize = 1)
    @GeneratedValue(generator = "SQCRUSC8_AUTHUSERHIST", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_AUTH_USER_ID", nullable = false)
    private AuthUser authUser;

    @NotNull
    @Column(name = "DT_DATA_MODIFICA")
    private ZonedDateTime dataModifica = null;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "TE_PASSWORD_HASH", length = 60)
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthUser getAuthUser() {
        return authUser;
    }

    public void setAuthUser(AuthUser authUser) {
        this.authUser = authUser;
    }

    public ZonedDateTime getDataModifica() {
        return dataModifica;
    }

    public void setDataModifica(ZonedDateTime dataModifica) {
        this.dataModifica = dataModifica;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "UserHistory [id=" + id + ", authUser=" + authUser + ", dataModifica=" + dataModifica + "]";
    }
}
