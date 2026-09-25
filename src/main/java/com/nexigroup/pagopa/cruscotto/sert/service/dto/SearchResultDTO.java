package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchResult.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO implements Serializable {
    private UUID instanceId;
    private UUID executionId;
    private String zipFileName;
    private String zipFilePath;
    private Long zipSizeBytes;
    private Long positionRows;
    private Long attemptRows;
    private Long transferRows;
    private Instant generatedAt;
    private Instant updatedAt;
}
