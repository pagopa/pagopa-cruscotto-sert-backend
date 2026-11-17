package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record IngestionDayStatusDTO(
    LocalDate arrivalDate,
    FdrIngestionDayStatus status,
    UUID executionId,
    Instant startedAt,
    Instant completedAt,
    Integer flowFiles,
    Integer iuvFiles,
    Long flowRows,
    Long iuvRows,
    String errorCode,
    String errorMessage,
    Instant updatedAt
) {}
