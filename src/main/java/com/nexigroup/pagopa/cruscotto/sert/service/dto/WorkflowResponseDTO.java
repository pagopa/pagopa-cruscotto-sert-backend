package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for WorkflowResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponseDTO implements Serializable {
    private Long count;

    @JsonProperty("events-position")
    private List<WorkflowObjectDTO> eventsPosition;

    @JsonProperty("events-token")
    private List<WorkflowTokenObjectDTO> eventsToken;
}
