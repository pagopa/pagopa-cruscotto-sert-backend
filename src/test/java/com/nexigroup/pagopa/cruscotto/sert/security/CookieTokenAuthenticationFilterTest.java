package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

class CookieTokenAuthenticationFilterTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final HandlerExceptionResolver exceptionResolver = mock(HandlerExceptionResolver.class);
    private final CookieTokenAuthenticationFilter filter = new CookieTokenAuthenticationFilter(
        mock(HttpSecurity.class),
        tokenProvider,
        exceptionResolver
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsRequestsWithoutCookiesAndPublicAuthenticationPath() throws Exception {
        MockHttpServletRequest requestWithoutCookies = request("/api/private");
        MockHttpServletRequest publicRequest = request("/api/authenticate");
        publicRequest.setCookies(new Cookie("unrelated", "value"));

        assertThat(shouldSkip(requestWithoutCookies)).isTrue();
        assertThat(shouldSkip(publicRequest)).isTrue();
    }

    @Test
    void authenticatesRequestUsingValidAccessTokenCookie() throws Exception {
        MockHttpServletRequest request = request("/api/private");
        request.setCookies(new Cookie(Constants.OIDC_ACCESS_TOKEN, "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "valid-token");
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getAuthentication("valid-token")).thenReturn(authentication);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        verify(chain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(request, response, null, null);
    }

    @Test
    void delegatesMissingTokenFailureButAllowsLogoutLoopToComplete() throws Exception {
        MockHttpServletRequest privateRequest = request("/api/private");
        privateRequest.setCookies(new Cookie("unrelated", "value"));
        MockHttpServletResponse privateResponse = new MockHttpServletResponse();
        filter.doFilter(privateRequest, privateResponse, mock(FilterChain.class));

        verify(exceptionResolver).resolveException(
            org.mockito.ArgumentMatchers.eq(privateRequest),
            org.mockito.ArgumentMatchers.eq(privateResponse),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.any(com.nexigroup.pagopa.cruscotto.sert.security.oauth2.JwtInvalid.class)
        );

        MockHttpServletRequest logoutRequest = request("/api/logout");
        logoutRequest.setCookies(new Cookie("unrelated", "value"));
        MockHttpServletResponse logoutResponse = new MockHttpServletResponse();
        FilterChain logoutChain = mock(FilterChain.class);
        filter.doFilter(logoutRequest, logoutResponse, logoutChain);

        verify(logoutChain).doFilter(logoutRequest, logoutResponse);
        verify(exceptionResolver, never()).resolveException(
            org.mockito.ArgumentMatchers.eq(logoutRequest),
            org.mockito.ArgumentMatchers.eq(logoutResponse),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        request.setServletPath(uri);
        return request;
    }

    private boolean shouldSkip(MockHttpServletRequest request) throws Exception {
        return filter.shouldNotFilter(request);
    }
}