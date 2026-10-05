package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class RestAuthenticationEntryPointTest {

    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

    @Test
    void returnsUnauthorizedForAuthenticationAndAuthorizationFailures() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        MockHttpServletResponse forbidden = new MockHttpServletResponse();

        entryPoint.commence(request, unauthorized, new BadCredentialsException("invalid"));
        entryPoint.handle(request, forbidden, new AccessDeniedException("denied"));

        assertThat(unauthorized.getStatus()).isEqualTo(401);
        assertThat(forbidden.getStatus()).isEqualTo(401);
        assertThat(unauthorized.getErrorMessage()).isEqualTo("Unauthorized");
        assertThat(forbidden.getErrorMessage()).isEqualTo("Unauthorized");
    }
}