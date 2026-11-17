package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

public enum FdrIngestionDayStatus {
    DISCOVERED,
    PROCESSING,
    VALIDATING,
    COMPLETED,
    FAILED,
    REINGESTION_REQUESTED,
    REINGESTING,
}
