package com.nexigroup.pagopa.cruscotto.sert.security;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.jwt.TokenProvider;
import com.nexigroup.pagopa.cruscotto.sert.security.oauth2.JwtInvalid;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class CookieTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CookieTokenAuthenticationFilter.class);

    private final RequestMatcher requestAuthenticate = new AntPathRequestMatcher("/api/authenticate");

    private final RequestMatcher requestResetPasswordInit = new AntPathRequestMatcher("/api/account/reset-password/init");

    private final RequestMatcher requestResetPasswordFinish = new AntPathRequestMatcher("/api/account/reset-password/finish");

    private final RequestMatcher requestAuthInfo = new AntPathRequestMatcher("/api/auth-info");
    private final RequestMatcher requestLanguage = new AntPathRequestMatcher("/api/language/**");
    private final RequestMatcher requestLogout = new AntPathRequestMatcher("/api/logout");

    private final RequestMatcher requestHealth = new AntPathRequestMatcher("/management/health");
    private final RequestMatcher requestInfo = new AntPathRequestMatcher("/management/info");
    private final RequestMatcher requestPrometheus = new AntPathRequestMatcher("/management/prometheus");

    private final RequestMatcher requestMelody = new AntPathRequestMatcher("/melody");

    private final RequestMatcher index = new AntPathRequestMatcher("/**/*.{js,html}");

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private final TokenProvider tokenProvider;
    private final HttpSecurity httpSecurity;

    private final HandlerExceptionResolver handlerExceptionResolver;

    public CookieTokenAuthenticationFilter(
        HttpSecurity httpSecurity,
        TokenProvider tokenProvider,
        HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.httpSecurity = httpSecurity;
        this.tokenProvider = tokenProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        LOGGER.info("Request: {}", request.getRequestURL());
        return (
            request.getCookies() == null ||
            this.requestAuthenticate.matches(request) ||
            this.requestLanguage.matches(request) ||
            this.index.matches(request) ||
            this.requestAuthInfo.matches(request) ||
            this.requestHealth.matches(request) ||
            this.requestInfo.matches(request) ||
            this.requestPrometheus.matches(request) ||
            this.requestResetPasswordInit.matches(request) ||
            this.requestResetPasswordFinish.matches(request) ||
            this.requestMelody.matches(request)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        LOGGER.info("Filtro CookieTokenAuthenticationFilter");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = loadAccessTokenFromCookie(request);

        LOGGER.info("Filtro Token {}", token);

        try {
            if (token == null) {
                throw new JwtInvalid();
            }

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            Authentication authenticationResult;

            if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
                authenticationResult = this.tokenProvider.getAuthentication(token);
                context.setAuthentication(authenticationResult);
                SecurityContextHolder.setContext(context);
            } else {
                BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);

                authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

                AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);

                authenticationResult = authenticationManager.authenticate(authenticationRequest);

                context.setAuthentication(authenticationResult);

                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException authenticationException) {
            this.logger.error("Authentication request with cookie failed", authenticationException);

            SecurityContextHolder.clearContext();
            Exception exception = authenticationException;

            if (authenticationException instanceof OAuth2AuthenticationException) {
                if (
                    ((OAuth2AuthenticationException) authenticationException).getError()
                        .getErrorCode()
                        .compareTo(JwtInvalid.FAILURE_MESSAGE) ==
                    0
                ) {
                    exception = new JwtInvalid(authenticationException);
                }
            }

            // se l'utente ha richiamato la logout non restituisco errore per non innescare un loop
            if (!this.requestLogout.matches(request)) {
                handlerExceptionResolver.resolveException(request, response, null, exception);
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    private String loadAccessTokenFromCookie(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> Constants.OIDC_ACCESS_TOKEN.equals(c.getName())).findAny();
            if (cookie.isPresent()) {
                token = cookie.orElseThrow().getValue();
            }
        }
        return token;
    }
}
