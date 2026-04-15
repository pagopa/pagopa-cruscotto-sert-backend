package com.nexigroup.pagopa.cruscotto.sert.security;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.helper.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizedClientCustomRepository
    implements org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository {

    @Override
    public void saveAuthorizedClient(
        OAuth2AuthorizedClient authorizedClient,
        Authentication principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        storeIntoCookies(response, authorizedClient);
    }

    @Override
    public void removeAuthorizedClient(
        String clientRegistrationId,
        Authentication principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        /*
         * This method is intentionally unsupported for this repository implementation.
         * Authorized clients are persisted into cookies via `storeIntoCookies` and are removed
         * by clearing the cookie during the application's logout/session cleanup flow,
         * which is implemented elsewhere. Throwing an exception makes it explicit to callers
         * that removal must be performed through the application's logout mechanism.
         */
        throw new UnsupportedOperationException("removeAuthorizedClient is not supported by OAuth2AuthorizedClientCustomRepository");

    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
        String clientRegistrationId,
        Authentication principal,
        HttpServletRequest request
    ) {
        return null;
    }

    private void storeIntoCookies(HttpServletResponse response, OAuth2AuthorizedClient authorizedClient) {
        response.addCookie(
            CookieHelper.generateCookie(Constants.OIDC_ACCESS_TOKEN, authorizedClient.getAccessToken().getTokenValue(), Duration.ofHours(1))
        );
    }
}
