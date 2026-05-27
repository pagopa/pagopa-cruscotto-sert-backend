package com.nexigroup.pagopa.cruscotto.sert.service.exception;

/**
 * Exception thrown when attempting to create a duplicate report for an instance.
 * Only one active report (PENDING, IN_PROGRESS, or COMPLETED) can exist per instance.
 */
public class DuplicateReportException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long instanceId;

    public DuplicateReportException(Long instanceId, String message) {
        super(message);
        this.instanceId = instanceId;
    }

    public Long getInstanceId() {
        return instanceId;
    }
}
