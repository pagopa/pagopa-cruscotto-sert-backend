package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private final HttpCookieOAuth2AuthorizationRequestRepository repository = new HttpCookieOAuth2AuthorizationRequestRepository();

    @Test
    void savesAndLoadsAuthorizationRequestAndRedirectCookie() {
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://identity.example.test/authorize")
            .clientId("client")
            .redirectUri("https://app.example.test/callback")
            .scopes(Set.of("openid"))
            .state("state-value")
            .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME))
            .thenReturn("https://app.example.test/after-login");
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        repository.saveAuthorizationRequest(authorizationRequest, request, response);
        verify(response, org.mockito.Mockito.times(2)).addCookie(cookieCaptor.capture());
        Cookie[] cookies = cookieCaptor.getAllValues().toArray(Cookie[]::new);
        when(request.getCookies()).thenReturn(cookies);

        assertThat(repository.loadAuthorizationRequest(request)).usingRecursiveComparison().isEqualTo(authorizationRequest);
        assertThat(cookies)
            .extracting(Cookie::getName)
            .contains(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME);
        assertThat(cookies).allSatisfy(cookie -> {
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
            assertThat(cookie.getMaxAge()).isEqualTo(180);
        });
    }

    @Test
    void removesAuthorizationCookiesWhenSavingNullRequest() {
        Cookie authorizationCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            "value"
        );
        Cookie redirectCookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, "value");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getCookies()).thenReturn(new Cookie[] { authorizationCookie, redirectCookie });

        repository.saveAuthorizationRequest(null, request, response);

        verify(response).addCookie(authorizationCookie);
        verify(response).addCookie(redirectCookie);
        assertThat(authorizationCookie.getMaxAge()).isZero();
        assertThat(redirectCookie.getMaxAge()).isZero();
    }

    @Test
    void returnsNullWhenNoAuthorizationCookieExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertThat(repository.loadAuthorizationRequest(request)).isNull();
        assertThat(repository.removeAuthorizationRequest(request, mock(HttpServletResponse.class))).isNull();
    }
}