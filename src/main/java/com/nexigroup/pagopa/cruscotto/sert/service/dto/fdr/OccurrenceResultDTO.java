package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

import java.util.UUID;

public record OccurrenceResultDTO(
    UUID occurrenceId,
    FdrSearchedBy searchedBy,
    String searchedValue,
    FlowRevisionSummaryDTO flow,
    ReportedPaymentRowDTO reportedPayment,
    int revisionNumber,
    int totalFlowRevisions
) {}
