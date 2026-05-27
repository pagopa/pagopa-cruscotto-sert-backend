package com.nexigroup.pagopa.cruscotto.sert.service.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;

public class CookieTranslateUtil {

    private CookieTranslateUtil() {}

    public static void create(HttpServletRequest request, HttpServletResponse response, String language) {
        Cookie cookie = WebUtils.getCookie(request, "NG_TRANSLATE_LANG_KEY");

        if (cookie == null) {
            cookie = new Cookie("NG_TRANSLATE_LANG_KEY", language);
        }

        cookie.setValue(language);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }
}
