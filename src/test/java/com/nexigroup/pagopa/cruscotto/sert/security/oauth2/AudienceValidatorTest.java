package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    @Test
    void acceptsJwtContainingAnAllowedAudience() {
        AudienceValidator validator = new AudienceValidator(List.of("api://sert", "api://other"));

        assertThat(validator.validate(jwt(List.of("api://unrelated", "api://sert"))).hasErrors()).isFalse();
    }

    @Test
    void rejectsJwtWithoutAnAllowedAudience() {
        AudienceValidator validator = new AudienceValidator(List.of("api://sert"));

        assertThat(validator.validate(jwt(List.of("api://other"))).getErrors())
            .extracting(error -> error.getDescription())
            .containsExactly("The required audience is missing");
    }

    @Test
    void requiresAtLeastOneAllowedAudience() {
        assertThatThrownBy(() -> new AudienceValidator(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Allowed audience should not be null or empty.");
    }

    private Jwt jwt(List<String> audience) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .audience(audience)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }
}