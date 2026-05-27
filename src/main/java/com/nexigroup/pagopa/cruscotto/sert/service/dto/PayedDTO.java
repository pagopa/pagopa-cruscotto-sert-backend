package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Payed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayedDTO implements Serializable {
    private String token;

    @JsonProperty("payment-born")
    private Instant paymentBorn;

    @JsonProperty("payed-date")
    private Instant payedDate;

    @JsonProperty("multi-outcome")
    private Boolean multiOutcome;
}
