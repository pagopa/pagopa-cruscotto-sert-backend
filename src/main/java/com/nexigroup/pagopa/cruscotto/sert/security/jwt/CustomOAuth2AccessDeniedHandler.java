package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;

@Setter
public class CustomOAuth2AccessDeniedHandler implements AccessDeniedHandler {

    public static final Logger logger = LoggerFactory.getLogger(CustomOAuth2AccessDeniedHandler.class);

    private String realmName;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        logger.error(e.getLocalizedMessage(), e);

        Map<String, String> parameters = new LinkedHashMap<>();

        String errorMessage = e.getLocalizedMessage();
        if (Objects.nonNull(realmName)) {
            parameters.put("realm", realmName);
        }

        if (request.getUserPrincipal() instanceof AbstractOAuth2TokenAuthenticationToken) {
            errorMessage = "The request requires higher privileges than provided by the access token.";
            parameters.put("error", "insufficient_scope");
            parameters.put("error_description", errorMessage);
            parameters.put("error_uri", "https://tools.ietf.org/html/rfc6750#section-3.1");
        }

        String message = RestResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message(errorMessage)
            .url(request.getRequestURI())
            .build()
            .toJson();

        String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
        response.addHeader("WWW-Authenticate", wwwAuthenticate);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(message);
    }

    private static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
        StringBuilder wwwAuthenticate = new StringBuilder();
        wwwAuthenticate.append("Bearer");
        if (!parameters.isEmpty()) {
            wwwAuthenticate.append(" ");
            int i = 0;

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                wwwAuthenticate.append((String) entry.getKey()).append("=\"").append((String) entry.getValue()).append("\"");
                if (i != parameters.size() - 1) {
                    wwwAuthenticate.append(", ");
                }

                ++i;
            }
        }

        return wwwAuthenticate.toString();
    }
}
