package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.security.GrantAuthoritiesLoad;
import com.nexigroup.pagopa.cruscotto.sert.security.oauth2.JwtInvalid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

class TokenProviderBehaviorTest {

    private final JwtEncoder encoder = mock(JwtEncoder.class);
    private final JwtDecoder decoder = mock(JwtDecoder.class);
    private final GrantAuthoritiesLoad authoritiesLoad = mock(GrantAuthoritiesLoad.class);
    private final TokenProvider provider = new TokenProvider(encoder, decoder, authoritiesLoad);
    private final Jwt validJwt = jwt();

    @Test
    void validatesTokenWhenDecoderAcceptsIt() {
        when(decoder.decode("valid-token")).thenReturn(validJwt);

        assertThat(provider.validateToken("valid-token")).isTrue();
    }

    @Test
    void rejectsInvalidTokenWhenDecoderThrows() {
        when(decoder.decode("invalid-token")).thenThrow(new JwtException("invalid"));

        assertThat(provider.validateToken("invalid-token")).isFalse();
    }

    @Test
    void buildsAuthenticationAndLoadsAuthoritiesFromClaims() {
        when(decoder.decode("valid-token")).thenReturn(validJwt);
        when(authoritiesLoad.load(validJwt.getClaims(), "subject", "123", "form"))
            .thenReturn(List.of(new SimpleGrantedAuthority("SERT.SEARCH")));

        var authentication = provider.getAuthentication("valid-token");

        assertThat(authentication.getName()).isEqualTo("subject");
        assertThat(authentication.getCredentials()).isEqualTo("valid-token");
        assertThat(authentication.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("SERT.SEARCH");
        verify(authoritiesLoad).load(validJwt.getClaims(), "subject", "123", "form");
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("valid-token")
            .header("alg", "none")
            .issuedAt(Instant.ofEpochSecond(123))
            .claim("sub", "subject")
            .claim("roles", List.of("ROLE_USER"))
            .build();
    }
}