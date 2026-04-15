package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationSuccessHandler.class);

    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthGroupRepository authGroupRepository;

    //    private final AuthUserService authUserService;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    public AuthenticationSuccessHandler(
        AuthenticationFailureHandler authenticationFailureHandler,
        AuthGroupRepository authGroupRepository,
        HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository
    ) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authGroupRepository = authGroupRepository;

        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        log.info("AuthenticationSuccessHandler.onAuthenticationSuccess");

        clearAuthenticationAttributes(request, response);

        Map<String, Object> attributes = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttributes();

        AuthUser user = getUser(attributes, ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId());

        GrantedAuthority groupAuthority = SecurityUtils.extractAuthorityFromClaims(attributes)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Group is not defined"));

        Optional<AuthGroup> authGroupOptional = authGroupRepository.findOneByNome(groupAuthority.getAuthority());

        AuthGroup authGroup = authGroupOptional.orElseThrow(() ->
            new IllegalArgumentException(String.format("Group %s not found into database", groupAuthority.getAuthority()))
        );

        user.setGroup(authGroup);

        handle(request, response, authentication);
    }

    private static AuthUser getUser(Map<String, Object> details, String authorizedClientRegistrationId) {
        AuthUser user = new AuthUser();

        if (details.get("preferred_username") != null) {
            user.setLogin(((String) details.get("preferred_username")) + "_" + authorizedClientRegistrationId);
        } else if (user.getLogin() == null) {
            user.setLogin(user.getId().toString() + "_" + authorizedClientRegistrationId);
        }

        if (details.get("given_name") != null) {
            user.setFirstName((String) details.get("given_name"));
        }
        if (details.get("family_name") != null) {
            user.setLastName((String) details.get("family_name"));
        }
        if (details.get("email_verified") != null) {
            user.setActivated((Boolean) details.get("email_verified"));
        }
        if (details.get("email") != null) {
            user.setEmail(((String) details.get("email")).toLowerCase());
        } else {
            user.setEmail((String) details.get("sub"));
        }
        if (details.get("langKey") != null) {
            user.setLangKey((String) details.get("langKey"));
        } else if (details.get("locale") != null) {
            // trim off country code if it exists
            String locale = (String) details.get("locale");
            if (locale.contains("_")) {
                locale = locale.substring(0, locale.indexOf('_'));
            } else if (locale.contains("-")) {
                locale = locale.substring(0, locale.indexOf('-'));
            }
            user.setLangKey(locale.toLowerCase());
        } else {
            // set langKey to default if not specified by IdP
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        }
        if (details.get("picture") != null) {
            user.setImageUrl((String) details.get("picture"));
        }

        String password = RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom());
        //        SecureRandom random = new SecureRandom(); // Compliant for security-sensitive use cases
        //        byte[] bytes = new byte[40];
        //        random.nextBytes(bytes);

        user.setCreatedBy(Constants.SYSTEM);
        user.setPassword(password);

        user.setActivated(true);
        user.setAuthenticationType(AuthenticationType.OAUHT2);
        return user;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
