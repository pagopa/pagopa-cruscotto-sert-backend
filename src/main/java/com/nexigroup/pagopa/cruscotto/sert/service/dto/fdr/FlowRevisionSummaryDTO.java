package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

public record FlowRevisionSummaryDTO(
    UUID revisionId,
    String flowId,
    String sourceUniqueId,
    Instant insertedTimestamp,
    LocalDate arrivalDate,
    int revisionOrdinal,
    int totalRevisions,
    boolean isLatestRevision,
    Instant flowDateTime,
    LocalDate settlementDate,
    String pspId,
    String pspIntermediaryId,
    Long paymentCount,
    BigDecimal paidAmount,
    FdrDataScope dataArea,
    String creditorInstitutionId,
    LocalDate dateInserted
) {}
