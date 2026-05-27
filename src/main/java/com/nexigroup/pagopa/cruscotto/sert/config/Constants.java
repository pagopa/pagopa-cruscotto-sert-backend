package com.nexigroup.pagopa.cruscotto.sert.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "it";
    public static final String ANONYMOUS_USER = "anonymoususer";

    public static final String ROLE_PASSWORD_EXPIRED = "ROLE_PASSWORD_EXPIRED";
    
    public static final String FUNCTION_CHANGE_PASSWORD_EXPIRED = "CHANGE_PASSWORD_EXPIRED";

    public static final String KEYCLOAK = "keycloak";

    public static final String FORM_LOGIN = "form";

    public static final String CLAIM_EXP = "exp";

    public static final String OIDC_REFRESH_TOKEN = "OIDC_REFRESH_TOKEN";
    public static final String OIDC_ACCESS_TOKEN = "OIDC_ACCESS_TOKEN";

    public static final String SPRING_PROFILE_OAUTH = "oauth";

    private Constants() {}
}
