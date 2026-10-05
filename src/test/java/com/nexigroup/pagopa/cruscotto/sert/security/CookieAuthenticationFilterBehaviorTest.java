package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

class CookieAuthenticationFilterBehaviorTest {

    private final HttpSecurity httpSecurity = mock(HttpSecurity.class);
    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final HandlerExceptionResolver exceptionResolver = mock(HandlerExceptionResolver.class);
    private final CookieTokenAuthenticationFilter filter = new CookieTokenAuthenticationFilter(
        httpSecurity,
        tokenProvider,
        exceptionResolver
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsRequestsWithoutCookiesAndPublicAuthenticateRoute() throws Exception {
        MockHttpServletRequest noCookieRequest = request("/api/private");
        assertThat(filter.shouldNotFilter(noCookieRequest)).isTrue();

        MockHttpServletRequest publicRequest = request("/api/authenticate");
        publicRequest.setCookies(new Cookie("other", "value"));
        assertThat(filter.shouldNotFilter(publicRequest)).isTrue();
    }

    @Test
    void authenticatesRequestFromValidAccessCookie() throws Exception {
        MockHttpServletRequest request = privateRequestWithAccessToken("signed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "signed-token");
        when(tokenProvider.validateToken("signed-token")).thenReturn(true);
        when(tokenProvider.getAuthentication("signed-token")).thenReturn(authentication);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        verify(chain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), isNull(), any());
    }

    @Test
    void delegatesInvalidAccessTokenToAuthenticationManager() throws Exception {
        MockHttpServletRequest request = privateRequestWithAccessToken("external-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken("external-user", "external-token");
        when(tokenProvider.validateToken("external-token")).thenReturn(false);
        when(httpSecurity.getSharedObject(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        verify(authenticationManager).authenticate(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    void handlesMissingTokenAndContinuesLogoutWithoutResolvingError() throws Exception {
        MockHttpServletRequest privateRequest = request("/api/private");
        privateRequest.setCookies(new Cookie("other", "value"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain privateChain = mock(FilterChain.class);

        filter.doFilter(privateRequest, response, privateChain);

        verify(exceptionResolver).resolveException(any(), any(), isNull(), any());
        verify(privateChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest logoutRequest = request("/api/logout");
        logoutRequest.setCookies(new Cookie("other", "value"));
        FilterChain logoutChain = mock(FilterChain.class);
        filter.doFilter(logoutRequest, response, logoutChain);

        verify(logoutChain).doFilter(logoutRequest, response);
    }

    private MockHttpServletRequest privateRequestWithAccessToken(String token) {
        MockHttpServletRequest request = request("/api/private");
        request.setCookies(new Cookie(Constants.OIDC_ACCESS_TOKEN, token));
        return request;
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        request.setServletPath(uri);
        return request;
    }
}