package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.access.AccessDeniedException;
import com.nexigroup.pagopa.cruscotto.sert.security.jwt.CustomBearerTokenAuthenticationEntryPoint;
import com.nexigroup.pagopa.cruscotto.sert.security.jwt.CustomOAuth2AccessDeniedHandler;

class OAuth2ResponseContractTest {

    @Test
    void writesPlainAndJsonErrorResponses() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseWriterUtil.writeResponse(response, "plain response");
        assertThat(response.getContentAsString()).isEqualTo("plain response");

        MockHttpServletResponse errorResponse = new MockHttpServletResponse();
        ResponseWriterUtil.writeErrorResponse(errorResponse, "failure");
        assertThat(errorResponse.getContentAsString()).contains("ERROR", "failure");
    }

    @Test
    void savesLoadsAndClearsAuthorizationRequestCookies() {
        HttpCookieOAuth2AuthorizationRequestRepository repository = new HttpCookieOAuth2AuthorizationRequestRepository();
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://idp.example.org/authorize")
            .clientId("sert-client")
            .redirectUri("https://app.example.org/callback")
            .state("state-value")
            .build();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, "https://app.example.org");
        MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveAuthorizationRequest(authorizationRequest, request, response);
        Cookie[] cookies = response.getCookies();
        assertThat(cookies).hasSize(2);
        request.setCookies(cookies);
        assertThat(repository.loadAuthorizationRequest(request)).usingRecursiveComparison().isEqualTo(authorizationRequest);
        assertThat(repository.removeAuthorizationRequest(request, response)).isNotNull();

        repository.removeAuthorizationRequestCookies(request, response);
        assertThat(cookies).allSatisfy(cookie -> assertThat(cookie.getMaxAge()).isZero());
    }

    @Test
    void authenticationEntryPointAndAccessDeniedHandlerWriteExpectedStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        CustomBearerTokenAuthenticationEntryPoint entryPoint = new CustomBearerTokenAuthenticationEntryPoint();
        entryPoint.setRealmName("sert-api");
        entryPoint.commence(request, unauthorized, new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "expired", null)));
        assertThat(unauthorized.getStatus()).isEqualTo(401);
        assertThat(unauthorized.getHeader("WWW-Authenticate")).contains("realm=\"sert-api\"", "error=\"invalid_token\"");
        assertThat(unauthorized.getContentAsString()).contains("Unauthenticated", "/api/private");

        MockHttpServletResponse forbidden = new MockHttpServletResponse();
        new CustomOAuth2AccessDeniedHandler().handle(request, forbidden, new AccessDeniedException("forbidden"));
        assertThat(forbidden.getStatus()).isEqualTo(403);
        assertThat(forbidden.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
        assertThat(forbidden.getContentAsString()).contains("forbidden", "/api/private");
    }
}