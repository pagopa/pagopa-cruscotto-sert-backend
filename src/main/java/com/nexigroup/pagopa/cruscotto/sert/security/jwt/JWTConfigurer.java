package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import com.nexigroup.pagopa.cruscotto.sert.security.CookieTokenAuthenticationFilter;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JWTConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TokenProvider tokenProvider;
    private final HttpSecurity httpSecurity;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JWTConfigurer(TokenProvider tokenProvider, HttpSecurity httpSecurity, HandlerExceptionResolver handlerExceptionResolver) {
        this.tokenProvider = tokenProvider;
        this.httpSecurity = httpSecurity;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void configure(HttpSecurity http) {
        CookieTokenAuthenticationFilter customFilter = new CookieTokenAuthenticationFilter(
            httpSecurity,
            tokenProvider,
            handlerExceptionResolver
        );
        http.addFilterBefore(customFilter, BearerTokenAuthenticationFilter.class);
    }
}
