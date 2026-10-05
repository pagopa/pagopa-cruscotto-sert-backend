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

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractsAuthoritiesWithGroupsClaimPrecedence() {
        List<String> roles = SecurityUtils.extractAuthorityFromClaims(
            Map.of("groups", List.of("group-admin"), "roles", List.of("legacy-role"))
        )
            .stream()
            .map(authority -> authority.getAuthority())
            .toList();

        assertThat(roles).containsExactly("group-admin");
        assertThat(SecurityUtils.extractAuthorityFromClaims(Map.of("roles", List.of("reader"))))
            .extracting(authority -> authority.getAuthority())
            .containsExactly("reader");
    }

    @Test
    void checksJwtAuthenticationAndExposesJwtClaims() {
        Jwt jwt = jwt();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(SecurityUtils.isAuthenticated()).isTrue();
        assertThat(SecurityUtils.isCurrentUserInRole("ROLE_USER")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN", "ROLE_USER")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasCurrentUserThisAuthority("ROLE_USER")).isTrue();
        assertThat(SecurityUtils.getCurrentUserJWT()).isEmpty();
        assertThat(SecurityUtils.getCurrentUserLogin()).contains("user@example.test");
        assertThat(SecurityUtils.getAuthenticationTypeUserLogin()).contains(AuthenticationType.ENTRA_ID);
        assertThat(SecurityUtils.getRolesFromJwt()).containsExactly("ROLE_USER");
        assertThat(SecurityUtils.getNameJwt()).isEqualTo("Test User");
        assertThat(SecurityUtils.getSubJwt()).isEqualTo("subject-1");
        assertThat(SecurityUtils.getPreferredUserName()).isEqualTo("user@example.test");
    }

    @Test
    void checksAuthoritiesFromNonJwtAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "credentials", List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")))
        );

        assertThat(SecurityUtils.isAuthenticated()).isTrue();
        assertThat(SecurityUtils.isCurrentUserInRole("ROLE_SUPPORT")).isTrue();
        assertThat(SecurityUtils.getCurrentUserJWT()).contains("credentials");
    }

    @Test
    void reportsNoAuthenticationWhenContextIsEmpty() {
        assertThat(SecurityUtils.getCurrentUserJWT()).isEmpty();
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
        assertThat(SecurityUtils.isCurrentUserInRole("ROLE_USER")).isFalse();
        assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_USER")).isFalse();
        assertThat(SecurityUtils.getSubJwt()).isNull();
        assertThat(SecurityUtils.getPreferredUserName()).isNull();
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("jwt-token")
            .header("alg", "none")
            .subject("subject-1")
            .claim("roles", List.of("ROLE_USER"))
            .claim("name", "Test User")
            .claim("preferred_username", "user@example.test")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }
}