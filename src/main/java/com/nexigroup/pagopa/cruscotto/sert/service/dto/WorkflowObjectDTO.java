package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for WorkflowObject
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowObjectDTO implements Serializable {
    private Instant insertedtimestamp;
    private String tipoevento;
    private String sottotipoevento;
    private String outcome;
    private String faultcode;
    private Integer positionNumber;

    @JsonProperty("event-id")
    private String eventId;
}
