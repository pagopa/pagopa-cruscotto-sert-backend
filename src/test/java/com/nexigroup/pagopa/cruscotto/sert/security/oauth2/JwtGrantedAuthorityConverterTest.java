package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.GrantAuthoritiesLoad;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class JwtGrantedAuthorityConverterTest {

    @Test
    void delegatesAuthorityResolutionUsingJwtClaimsAndMetadata() {
        GrantAuthoritiesLoad grantAuthoritiesLoad = mock(GrantAuthoritiesLoad.class);
        JwtGrantedAuthorityConverter converter = new JwtGrantedAuthorityConverter(grantAuthoritiesLoad);
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject-1")
            .claim("groups", List.of("group-1"))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(600))
            .build();
        when(grantAuthoritiesLoad.load(jwt.getClaims(), "subject-1", "1767225600", Constants.FORM_LOGIN))
            .thenReturn(List.of(new SimpleGrantedAuthority("SERT.SEARCH")));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token.getToken()).isSameAs(jwt);
        assertThat(token.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("SERT.SEARCH");
        verify(grantAuthoritiesLoad).load(jwt.getClaims(), "subject-1", "1767225600", Constants.FORM_LOGIN);
    }
}