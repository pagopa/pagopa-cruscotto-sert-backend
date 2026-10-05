package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityContextBehaviorTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsLoginAndClaimsFromJwtPrincipal() {
        Jwt jwt = jwt(Map.of("sub", "oidc-subject", "preferred_username", "user@example.org", "name", "Example User"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertThat(SecurityUtils.getCurrentUserLogin()).contains("user@example.org");
        assertThat(SecurityUtils.getPreferredUserName()).isEqualTo("user@example.org");
        assertThat(SecurityUtils.getNameJwt()).isEqualTo("Example User");
        assertThat(SecurityUtils.getSubJwt()).isEqualTo("oidc-subject");
        assertThat(SecurityUtils.getAuthenticationTypeUserLogin()).contains(AuthenticationType.ENTRA_ID);
    }

    @Test
    void evaluatesAuthoritiesForClassicAndJwtAuthentication() {
        SecurityContextHolder.clearContext();
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
        assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities("ROLE_ADMIN")).isTrue();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "token", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        assertThat(SecurityUtils.isAuthenticated()).isTrue();
        assertThat(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_USER", "ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities("ROLE_ADMIN")).isFalse();
        assertThat(SecurityUtils.getCurrentUserJWT()).contains("token");

        Jwt jwt = jwt(Map.of("sub", "jwt-user", "groups", List.of("ROLE_JWT")));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertThat(SecurityUtils.isCurrentUserInRole("ROLE_JWT")).isTrue();
        assertThat(SecurityUtils.extractAuthorityFromClaims(Map.of("groups", List.of("G1"), "roles", List.of("R1"))))
            .extracting("authority")
            .containsExactly("G1");
        assertThat(SecurityUtils.extractAuthorityFromClaims(Map.of("roles", List.of("R1"))))
            .extracting("authority")
            .containsExactly("R1");
    }

    private Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .issuedAt(Instant.now())
            .claim("sub", claims.getOrDefault("sub", "subject"))
            .claims(allClaims -> allClaims.putAll(claims))
            .build();
    }
}