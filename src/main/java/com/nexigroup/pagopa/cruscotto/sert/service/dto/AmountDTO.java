package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Amount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountDTO implements Serializable {
    private Double fee;
    private Double amount;
}
