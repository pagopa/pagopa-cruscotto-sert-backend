package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class AuthenticationFailureHandlerTest {

    @Test
    void clearsAuthenticationCookiesAndRedirectsToConfiguredFailureUrl() throws Exception {
        AuthenticationFailureHandler handler = new AuthenticationFailureHandler(new HttpCookieOAuth2AuthorizationRequestRepository());
        handler.setDefaultFailureUrl("/login?error");
        Cookie accessToken = new Cookie(Constants.OIDC_ACCESS_TOKEN, "token");
        Cookie authorizationRequest = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            "serialized"
        );
        Cookie redirectUri = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, "/home");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/callback");
        request.setCookies(accessToken, authorizationRequest, redirectUri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("invalid credentials"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error");
        assertThat(accessToken.getMaxAge()).isZero();
        assertThat(authorizationRequest.getMaxAge()).isZero();
        assertThat(redirectUri.getMaxAge()).isZero();
    }
}