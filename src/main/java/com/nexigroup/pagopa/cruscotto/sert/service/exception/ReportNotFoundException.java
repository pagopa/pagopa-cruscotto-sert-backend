package com.nexigroup.pagopa.cruscotto.sert.service.exception;

/**
 * Exception thrown when a requested report is not found.
 */
public class ReportNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReportNotFoundException(Long reportId) {
        super("Report not found: " + reportId);
    }

    public ReportNotFoundException(String message) {
        super(message);
    }
}
