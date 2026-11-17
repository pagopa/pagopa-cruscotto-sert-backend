package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record FlowRevisionDetailDTO(
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
    LocalDate dateInserted,
    String reason,
    List<Instant> allDates,
    List<String> availableFilters
) {

    public static FlowRevisionDetailDTO from(FlowRevisionSummaryDTO summary, String reason, List<Instant> allDates, List<String> availableFilters) {
        return new FlowRevisionDetailDTO(
            summary.revisionId(),
            summary.flowId(),
            summary.sourceUniqueId(),
            summary.insertedTimestamp(),
            summary.arrivalDate(),
            summary.revisionOrdinal(),
            summary.totalRevisions(),
            summary.isLatestRevision(),
            summary.flowDateTime(),
            summary.settlementDate(),
            summary.pspId(),
            summary.pspIntermediaryId(),
            summary.paymentCount(),
            summary.paidAmount(),
            summary.dataArea(),
            summary.creditorInstitutionId(),
            summary.dateInserted(),
            reason,
            allDates,
            availableFilters
        );
    }
}
