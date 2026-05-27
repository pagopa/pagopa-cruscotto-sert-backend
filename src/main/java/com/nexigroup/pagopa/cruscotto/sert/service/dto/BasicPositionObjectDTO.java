package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for BasicPositionObject
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasicPositionObjectDTO implements Serializable {
    private String nav;

    @JsonProperty("pa-emittente")
    private String paEmittente;
}
