package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for TokenInfo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoDTO implements Serializable {
    @JsonProperty("position-info")
    private PositionPaymentInfoDTO positionInfo;

    @JsonProperty("is-payed-token")
    private Boolean isPayedToken;

    private PayedDTO payed;
    private ActorsDTO actors;
    private AmountDTO amount;

    @JsonProperty("payment-info")
    private PaymentInfoDTO paymentInfo;
}
