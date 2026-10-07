package com.nexigroup.pagopa.cruscotto.sert.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthUserDomainContractTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void createValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidator() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsUserWithValidRequiredFields() {
        AuthUser user = validUser();

        assertThat(validator.validate(user)).isEmpty();
    }

    @Test
    void reportsInvalidLoginPasswordEmailAndLanguage() {
        AuthUser user = validUser();
        user.setLogin("not a login");
        user.setPassword("short");
        user.setEmail("not-an-email");
        user.setLangKey("e");

        Set<String> invalidProperties = validator.validate(user)
            .stream()
            .map(violation -> violation.getPropertyPath().toString())
            .collect(Collectors.toSet());

        assertThat(invalidProperties).contains("login", "password", "email", "langKey");
    }

    @Test
    void identityUsesPersistentIdAndDefaultsPasswordExpiry() {
        AuthUser first = new AuthUser();
        AuthUser second = new AuthUser();

        assertThat(first.getPasswordExpiredDay()).isEqualTo(AuthUser.DEFAULT_DAY_PASSWORD_EXPIRED);
        assertThat(first).isNotEqualTo(second);
        first.setId(10L);
        second.setId(10L);
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(new Object());
    }

    private AuthUser validUser() {
        AuthUser user = new AuthUser();
        user.setLogin("user@example.org");
        user.setPassword("a-secure-password");
        user.setEmail("user@example.org");
        user.setLangKey("it");
        user.setAuthenticationType(AuthenticationType.FORM_LOGIN);
        return user;
    }
}