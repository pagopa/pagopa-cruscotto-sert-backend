package com.nexigroup.pagopa.cruscotto.sert.service.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.InvalidPasswordException;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserHistoryService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordValidatorTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthUserService authUserService = mock(AuthUserService.class);
    private final AuthUserHistoryService historyService = mock(AuthUserHistoryService.class);
    private final PasswordValidator validator = new PasswordValidator(passwordEncoder, authUserService, historyService);

    @Test
    void acceptsPasswordMeetingComplexityRules() {
        when(authUserService.findUserByLogin("user")).thenReturn(Optional.empty());

        assertThatCode(() -> validator.check("user", "Abcdef1!", "John", "Smith")).doesNotThrowAnyException();
    }

    @Test
    void rejectsWeakPasswordAndPasswordContainingPersonalData() {
        when(authUserService.findUserByLogin("user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.check("user", "weak", "John", "Smith"))
            .isInstanceOf(InvalidPasswordException.class);
        assertThatThrownBy(() -> validator.check("user", "User1234!", "John", "Smith"))
            .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void rejectsPasswordMatchingCurrentOrHistoricalPassword() {
        AuthUser user = new AuthUser();
        user.setId(25L);
        user.setPassword("encoded-current");
        when(authUserService.findUserByLogin("user")).thenReturn(Optional.of(user));
        when(historyService.getOldPassword(25L, 6)).thenReturn(new String[] { "encoded-old" });
        when(passwordEncoder.matches("Abcdef1!", "encoded-current")).thenReturn(false);
        when(passwordEncoder.matches("Abcdef1!", "encoded-old")).thenReturn(true);

        assertThatThrownBy(() -> validator.check("user", "Abcdef1!", "John", "Smith"))
            .isInstanceOf(InvalidPasswordException.class);
    }
}