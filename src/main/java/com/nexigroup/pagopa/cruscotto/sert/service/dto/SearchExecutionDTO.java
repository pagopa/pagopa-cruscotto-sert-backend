package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchExecution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchExecutionDTO implements Serializable {
    private UUID id;
    private UUID instanceId;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private Long totalInputRows;
    private Long processedRows;
    private Integer generatedFiles;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
