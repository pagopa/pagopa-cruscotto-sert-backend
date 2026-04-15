package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PaymentInfo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDTO implements Serializable {
    private String touchpoint; // enum in YAML: Checkout, Touchpoint PSP, Redirect, DW, Pagamento POS

    @JsonProperty("payment-method")
    private String paymentMethod;

    @JsonProperty("is-dw")
    private Boolean isDw;

    @JsonProperty("is-gpd")
    private Boolean isGpd;

    @JsonProperty("is-standin")
    private Boolean isStandin;

    @JsonProperty("is-cart")
    private Boolean isCart;
}
