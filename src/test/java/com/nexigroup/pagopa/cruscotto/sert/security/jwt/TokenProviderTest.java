package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.security.GrantAuthoritiesLoad;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

class TokenProviderTest {

    private final JwtDecoder decoder = mock(JwtDecoder.class);
    private final GrantAuthoritiesLoad authoritiesLoad = mock(GrantAuthoritiesLoad.class);
    private final TokenProvider tokenProvider = new TokenProvider(mock(JwtEncoder.class), decoder, authoritiesLoad);

    @Test
    void validatesTokensWhenDecoderAcceptsThem() {
        when(decoder.decode("valid-token")).thenReturn(jwt());

        assertThat(tokenProvider.validateToken("valid-token")).isTrue();
    }

    @Test
    void rejectsTokensWhenDecoderThrowsJwtException() {
        when(decoder.decode("invalid-token")).thenThrow(new JwtException("invalid"));

        assertThat(tokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    void buildsAuthenticationFromDecodedClaimsAndResolvedAuthorities() {
        Jwt jwt = mock(Jwt.class);
        Map<String, Object> claims = Map.of("sub", "subject-1", "iat", 1767225600L);
        when(decoder.decode("access-token")).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(claims);
        when(authoritiesLoad.load(claims, "subject-1", "1767225600", "form"))
            .thenReturn(List.of(new SimpleGrantedAuthority("SERT.SEARCH")));

        Authentication authentication = tokenProvider.getAuthentication("access-token");

        assertThat(authentication.getPrincipal()).isInstanceOf(User.class);
        assertThat(((User) authentication.getPrincipal()).getUsername()).isEqualTo("subject-1");
        assertThat(authentication.getCredentials()).isEqualTo("access-token");
        assertThat(authentication.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("SERT.SEARCH");
        verify(authoritiesLoad).load(claims, "subject-1", "1767225600", "form");
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("valid-token")
            .header("alg", "none")
            .subject("subject")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }
}