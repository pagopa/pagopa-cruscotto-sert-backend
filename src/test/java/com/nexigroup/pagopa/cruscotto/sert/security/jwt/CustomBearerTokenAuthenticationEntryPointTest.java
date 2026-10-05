package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.http.HttpStatus;

class CustomBearerTokenAuthenticationEntryPointTest {

    @Test
    void writesUnauthorizedJsonAndRealmHeaderForOrdinaryAuthenticationFailure() throws Exception {
        CustomBearerTokenAuthenticationEntryPoint entryPoint = new CustomBearerTokenAuthenticationEntryPoint();
        entryPoint.setRealmName("sert-api");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad credentials"));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer realm=\"sert-api\"");
        assertThat(response.getContentAsString()).contains("Unauthenticated", "bad credentials", "/api/private");
    }

    @Test
    void includesOauthErrorMetadataInBearerChallenge() throws Exception {
        CustomBearerTokenAuthenticationEntryPoint entryPoint = new CustomBearerTokenAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();
        BearerTokenError error = new BearerTokenError(
            "invalid_token",
            HttpStatus.UNAUTHORIZED,
            "expired",
            "https://errors.example.test/token"
        );

        entryPoint.commence(request, response, new OAuth2AuthenticationException(error));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getHeader("WWW-Authenticate"))
            .contains("error=\"invalid_token\"", "error_description=\"expired\"", "error_uri=\"https://errors.example.test/token\"");
    }
}