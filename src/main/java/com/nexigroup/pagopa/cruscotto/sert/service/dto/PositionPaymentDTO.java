package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PositionPayment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionPaymentDTO implements Serializable {
    @JsonProperty("position-info")
    private PositionPaymentInfoDTO positionInfo;

    private long tokens;

    @JsonProperty("all-tokens")
    private List<String> allTokens;

    private PayedDTO payed;
    private ActorsDTO actors;
    private AmountDTO amount;

    @JsonProperty("payment-info")
    private PaymentInfoDTO paymentInfo;
}
