package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class PreDecodeJwtDecoderTest {

    private final JwtDecoder delegate = mock(JwtDecoder.class);
    private final PreDecodeJwtDecoder decoder = new PreDecodeJwtDecoder(delegate);

    @Test
    void decodeExtractsClaimsAndConvertsTimestamps() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        Instant issuedAt = Instant.now().minusSeconds(30);
        String token = tokenWithPayload(
            "{\"sub\":\"user-1\",\"exp\":" + expiresAt.getEpochSecond() + ",\"iat\":" + issuedAt.getEpochSecond() + "}"
        );

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("user-1");
        assertThat(jwt.getExpiresAt()).isEqualTo(Instant.ofEpochSecond(expiresAt.getEpochSecond()));
        assertThat(jwt.getIssuedAt()).isEqualTo(Instant.ofEpochSecond(issuedAt.getEpochSecond()));
        assertThat(jwt.getHeaders()).containsEntry("alg", "none");
        verifyNoInteractions(delegate);
    }

    @Test
    void decodeRejectsMalformedToken() {
        assertThatThrownBy(() -> decoder.decode("not-a-jwt"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Errore durante l'estrazione delle claims dal JWT");
    }

    @Test
    void decodeRejectsExpiredToken() {
        String token = tokenWithPayload("{\"sub\":\"user-1\",\"exp\":1}");

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    private String tokenWithPayload(String json) {
        String payload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return "e30." + payload + ".";
    }
}