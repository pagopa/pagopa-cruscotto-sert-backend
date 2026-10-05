package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.security.helper.CookieHelper;
import com.nexigroup.pagopa.cruscotto.sert.security.helper.CookieUtils;
import com.nexigroup.pagopa.cruscotto.sert.security.oauth2.AudienceValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class CookieAndAudienceBehaviorTest {

    @Test
    void cookieHelperFindsCaseInsensitiveNameAndBuildsSecureExpiryCookies() {
        Cookie original = new Cookie("Session", "payload");

        assertThat(CookieHelper.retrieveCookie(new Cookie[] { original }, "session")).contains(original);
        assertThat(CookieHelper.retrieve(new Cookie[] { original }, "SESSION")).contains("payload");
        assertThat(CookieHelper.retrieve(null, "session")).isEmpty();

        Cookie generated = CookieHelper.generateCookie("Session", "payload", Duration.ofMinutes(3));
        assertThat(generated.getPath()).isEqualTo("/");
        assertThat(generated.isHttpOnly()).isTrue();
        assertThat(generated.getMaxAge()).isEqualTo(180);
        assertThat(generated.getSecure()).isFalse();
        assertThat(CookieHelper.generateExpiredCookie("Session").getMaxAge()).isZero();
    }

    @Test
    void cookieUtilsReadsAddsDeletesAndSerializesCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie accessCookie = new Cookie("access", "value");
        when(request.getCookies()).thenReturn(new Cookie[] { accessCookie });

        assertThat(CookieUtils.getCookie(request, "access")).contains(accessCookie);
        assertThat(CookieUtils.getCookie(request, "missing")).isEmpty();
        CookieUtils.addCookie(response, "refresh", "new-value", 60);
        verify(response).addCookie(org.mockito.ArgumentMatchers.argThat(cookie ->
            cookie.getName().equals("refresh") && cookie.isHttpOnly() && cookie.getSecure() && cookie.getMaxAge() == 60
        ));

        CookieUtils.deleteCookie(request, response, "access");
        assertThat(accessCookie.getValue()).isEmpty();
        assertThat(accessCookie.getMaxAge()).isZero();

        String serialized = CookieUtils.serialize("state");
        assertThat(CookieUtils.deserialize(new Cookie("state", serialized), String.class)).isEqualTo("state");
    }

    @Test
    void audienceValidatorAcceptsAnyAllowedAudienceAndRejectsMissingConfiguration() {
        AudienceValidator validator = new AudienceValidator(List.of("api-a", "api-b"));
        assertThat(validator.validate(jwt(List.of("other", "api-b"))).hasErrors()).isFalse();
        OAuth2TokenValidatorResult rejected = validator.validate(jwt(List.of("other")));
        assertThat(rejected.hasErrors()).isTrue();
        assertThat(rejected.getErrors()).extracting("errorCode").containsExactly("invalid_token");

        assertThatThrownBy(() -> new AudienceValidator(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    private Jwt jwt(List<String> audience) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuedAt(Instant.now())
            .audience(audience)
            .build();
    }
}