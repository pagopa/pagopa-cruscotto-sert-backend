package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record IngestionEventDTO(
    Long eventId,
    LocalDate arrivalDate,
    UUID executionId,
    Instant eventTimestamp,
    String eventCode,
    FdrEventLevel level,
    FdrEventSourceCode sourceCode,
    String fileName,
    String blobEtag,
    Long rowsRead,
    Long rowsWritten,
    Long rowsRejected,
    Long durationMs,
    String errorCode,
    String errorMessage,
    Map<String, Object> details
) {}
