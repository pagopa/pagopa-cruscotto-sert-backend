package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PositionPaymentInfo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PositionPaymentInfoDTO extends BasicPositionObjectDTO implements Serializable {
    private String iuv;

    @JsonProperty("creditor-reference-id")
    private String creditorReferenceId;

    @JsonProperty("last-event")
    private Instant lastEvent;

    @JsonProperty("is-cached")
    private Boolean isCached;
}
