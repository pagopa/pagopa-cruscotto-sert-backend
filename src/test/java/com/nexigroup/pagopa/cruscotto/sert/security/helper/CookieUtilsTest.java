package com.nexigroup.pagopa.cruscotto.sert.security.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CookieUtilsTest {

    @Test
    void findsNamedCookieAndHandlesMissingCookieArray() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie wanted = new Cookie("wanted", "value");
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("other", "x"), wanted });

        assertThat(CookieUtils.getCookie(request, "wanted")).contains(wanted);
        when(request.getCookies()).thenReturn(null);
        assertThat(CookieUtils.getCookie(request, "wanted")).isEmpty();
    }

    @Test
    void addsCookieWithSecurityAttributesAndDeletesMatchingCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie existing = new Cookie("session", "token");
        when(request.getCookies()).thenReturn(new Cookie[] { existing });

        CookieUtils.addCookie(response, "new-cookie", "new-value", 120);
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        Cookie created = captor.getValue();
        assertThat(created.getName()).isEqualTo("new-cookie");
        assertThat(created.getValue()).isEqualTo("new-value");
        assertThat(created.getPath()).isEqualTo("/");
        assertThat(created.isHttpOnly()).isTrue();
        assertThat(created.getSecure()).isTrue();
        assertThat(created.getMaxAge()).isEqualTo(120);

        CookieUtils.deleteCookie(request, response, "session");

        assertThat(existing.getValue()).isEmpty();
        assertThat(existing.getPath()).isEqualTo("/");
        assertThat(existing.getSecure()).isTrue();
        assertThat(existing.getMaxAge()).isZero();
        verify(response, org.mockito.Mockito.times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void serializesAndDeserializesSerializableValues() {
        List<String> original = List.of("one", "two");
        Cookie cookie = new Cookie("payload", CookieUtils.serialize(original));

        assertThat(CookieUtils.deserialize(cookie, List.class)).containsExactly("one", "two");
    }
}