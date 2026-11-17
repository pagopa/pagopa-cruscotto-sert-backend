package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReingestionBatchDTO(
    UUID requestId,
    FdrAsyncStatus status,
    Instant createdAt,
    Instant startedAt,
    Instant completedAt,
    List<ReingestionDayItemDTO> items
) {}
