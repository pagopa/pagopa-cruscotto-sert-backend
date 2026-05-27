package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import org.springframework.security.core.AuthenticationException;

public class JwtInvalid extends AuthenticationException {

    public static final String FAILURE_MESSAGE = "invalid_token";

    public JwtInvalid() {
        super(FAILURE_MESSAGE);
    }

    public JwtInvalid(Throwable t) {
        super(FAILURE_MESSAGE, t);
    }
}
