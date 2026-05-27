package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A DTO for the {@link AuthPermission} entity.
 */
@ToString
@EqualsAndHashCode
public class AuthPermissionDTO implements Serializable {

    private static final long serialVersionUID = -4114452898364238359L;

    private Long id;

    @NotEmpty
    @Size(max = 100)
    private String nome;

    @NotEmpty
    @Size(max = 50)
    private String modulo;

    private String type = "permesso";

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

    public String getType() {
        return type;
    }
}
