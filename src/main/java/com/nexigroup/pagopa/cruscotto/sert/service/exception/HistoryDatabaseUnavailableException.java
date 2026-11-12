package com.nexigroup.pagopa.cruscotto.sert.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "DB History unavailable")
public class HistoryDatabaseUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "DB History unavailable";

    public HistoryDatabaseUnavailableException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
