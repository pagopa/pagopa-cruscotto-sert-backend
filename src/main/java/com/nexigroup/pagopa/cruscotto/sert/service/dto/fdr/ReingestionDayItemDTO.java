package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReingestionDayItemDTO(
    LocalDate arrivalDate,
    int sequence,
    FdrReingestionDayStatus status,
    UUID executionId,
    Instant startedAt,
    Instant completedAt,
    String errorCode,
    String errorMessage
) {}
