package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

public record ReportedPaymentRowDTO(
    UUID recordId,
    UUID revisionId,
    String flowId,
    String sourceUniqueId,
    String iuv,
    String iur,
    BigDecimal amount,
    String outcomeCode,
    Instant singlePaymentOutcomeTimestamp,
    Integer idsp,
    Instant flowDateTime,
    String pspId,
    String pspIntermediaryId,
    Instant insertedTimestamp,
    String creditorInstitutionId,
    LocalDate dateInserted
) {}
