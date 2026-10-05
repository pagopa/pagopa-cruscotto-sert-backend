package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SpringSecurityAuditorAwareTest {

    private final SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesSystemWhenNoUserIsAuthenticated() {
        assertThat(auditorAware.getCurrentAuditor()).contains("system");
    }

    @Test
    void usesPreferredUsernameForJwtAuditor() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .claim("preferred_username", "auditor@example.test")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(auditorAware.getCurrentAuditor()).contains("auditor@example.test");
    }
}