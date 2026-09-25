package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchExecutionStep.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchExecutionStepDTO implements Serializable {

    private UUID id;
    private UUID executionId;
    private UUID instanceId;
    private String phase;
    private Integer attemptNo;
    private String status;
    private LocalDateTime windowFrom;
    private LocalDateTime windowTo;
    private Long rowsProcessed;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationMs;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
}