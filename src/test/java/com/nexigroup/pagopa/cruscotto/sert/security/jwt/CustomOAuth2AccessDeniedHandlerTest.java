package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class CustomOAuth2AccessDeniedHandlerTest {

    @Test
    void writesInsufficientScopeChallengeForJwtPrincipal() throws Exception {
        CustomOAuth2AccessDeniedHandler handler = new CustomOAuth2AccessDeniedHandler();
        handler.setRealmName("sert-api");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secure");
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        request.setUserPrincipal(new JwtAuthenticationToken(jwt));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new org.springframework.security.access.AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("WWW-Authenticate"))
            .contains("realm=\"sert-api\"", "error=\"insufficient_scope\"");
        assertThat(response.getContentAsString()).contains("higher privileges", "/api/secure");
    }

    @Test
    void preservesOrdinaryAccessDeniedMessageWithoutTokenPrincipal() throws Exception {
        CustomOAuth2AccessDeniedHandler handler = new CustomOAuth2AccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secure");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("not permitted"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
        assertThat(response.getContentAsString()).contains("not permitted");
    }
}