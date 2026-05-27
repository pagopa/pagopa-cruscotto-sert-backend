package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for TransferObject
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferObjectDTO implements Serializable {
    private Integer idTransfer;

    @JsonProperty("type-transfer")
    private String typeTransfer; // enum: sepa, bollo

    private String iban;
    private Double amount;

    @JsonProperty("pa-fiscal-code")
    private String paFiscalCode;
}
