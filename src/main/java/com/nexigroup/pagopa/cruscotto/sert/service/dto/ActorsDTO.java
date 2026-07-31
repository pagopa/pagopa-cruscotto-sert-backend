package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Actors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorsDTO implements Serializable {
    private String psp;

    @JsonProperty("psp-description")
    private String pspDescription;

    @JsonProperty("pt-pa")
    private String ptPa;

    @JsonProperty("pt-pa-description")
    private String ptPaDescription;

    @JsonProperty("pt-psp")
    private String ptPsp;

    @JsonProperty("pt-psp-description")
    private String ptPspDescription;

    private String station;
    private String channel;
}
