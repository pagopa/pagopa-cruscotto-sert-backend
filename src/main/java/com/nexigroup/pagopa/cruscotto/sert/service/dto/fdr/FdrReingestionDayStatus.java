package com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr;

public enum FdrReingestionDayStatus {
    REQUESTED,
    QUEUED,
    DELETING_PARTITIONS,
    INGESTING,
    VALIDATING,
    COMPLETED,
    FAILED,
    CANCELLED,
}
