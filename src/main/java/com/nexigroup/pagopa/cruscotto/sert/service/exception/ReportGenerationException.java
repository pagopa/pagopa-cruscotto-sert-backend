package com.nexigroup.pagopa.cruscotto.sert.service.exception;

/**
 * Exception thrown when report generation fails.
 */
public class ReportGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReportGenerationException(String message) {
        super(message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
