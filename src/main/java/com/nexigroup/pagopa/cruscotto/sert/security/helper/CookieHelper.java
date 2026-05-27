package com.nexigroup.pagopa.cruscotto.sert.security.helper;

import static java.util.Objects.isNull;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.Optional;

public class CookieHelper {

    private static final String COOKIE_DOMAIN = "localhost";
    private static final Boolean HTTP_ONLY = Boolean.TRUE;
    private static final Boolean SECURE = Boolean.FALSE; // in produzione deve essere impostato a true

    public static Optional<Cookie> retrieveCookie(Cookie[] cookies, String name) {
        if (isNull(cookies)) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return Optional.ofNullable(cookie);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> retrieve(Cookie[] cookies, String name) {
        if (isNull(cookies)) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    public static Cookie generateCookie(String name, String value, Duration maxAge) {
        Cookie cookie = new Cookie(name, value);
        if (!COOKIE_DOMAIN.equals(COOKIE_DOMAIN)) {
            cookie.setDomain(COOKIE_DOMAIN);
        }
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setMaxAge((int) maxAge.getSeconds());
        cookie.setPath("/");

        return cookie;
    }

    public static Cookie generateExpiredCookie(String name) {
        return generateCookie(name, "-", Duration.ZERO);
    }
}
