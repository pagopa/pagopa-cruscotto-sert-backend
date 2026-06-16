package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for TransferPayment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferPaymentDTO implements Serializable {
    @JsonProperty("position-info")
    private PositionPaymentInfoDTO positionInfo;

    private String token;

    @JsonProperty("transfers-count")
    private Double transfersCount;

    private List<TransferObjectDTO> transfers;
}
