package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A DTO for the {@link AuthFunction} entity.
 */
@ToString
@EqualsAndHashCode
public class AuthFunctionDTO implements Serializable {

    private static final long serialVersionUID = -5753709660115158639L;

    private Long id;

    @NotEmpty
    @Size(max = 50)
    private String nome;

    @NotEmpty
    @Size(max = 50)
    private String modulo;

    @NotEmpty
    @Size(max = 200)
    private String descrizione;

    private String type = "funzione";

    private Long groupId;

    private Boolean selected = null;

    private Set<AuthPermissionDTO> authPermissions = new HashSet<>();

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

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Set<AuthPermissionDTO> getAuthPermissions() {
        return authPermissions;
    }

    public void setAuthPermissions(Set<AuthPermissionDTO> authPermissions) {
        this.authPermissions = authPermissions;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getType() {
        return type;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
