package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserAccountDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PasswordValidator;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.EmailAlreadyUsedException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AccountResourceTest {

    private final AuthUserService authUserService = mock(AuthUserService.class);
    private final AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
    private final PasswordValidator passwordValidator = mock(PasswordValidator.class);
    private final ApplicationProperties properties = mock(ApplicationProperties.class);
    private final AccountResource resource = new AccountResource(
        authUserService,
        authUserRepository,
        passwordValidator,
        mock(com.nexigroup.pagopa.cruscotto.sert.service.impl.MailService.class),
        properties
    );

    @Test
    void returnsAccountFromJwt() {
        Jwt jwt = jwt();
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt, List.of());
        AuthUserDTO dto = userDto();
        when(authUserService.getUserWithAuthoritiesFromJwt(List.of("group-1"), List.of("group-1"))).thenReturn(Set.of("function.read"));
        when(authUserService.buildFromJwt(jwt, Set.of("function.read"))).thenReturn(dto);

        ResponseEntity<AuthUserDTO> response = resource.getAccount(null, null, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(authUserService).buildFromJwt(jwt, Set.of("function.read"));
    }

    @Test
    void rejectsNonJwtPrincipal() {
        assertThatThrownBy(() -> resource.getAccount(null, null, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("JWT principal not found");
    }

    @Test
    void rejectsAccountModificationForOAuthUser() {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getAuthenticationTypeUserLogin).thenReturn(java.util.Optional.of(AuthenticationType.OAUHT2));

            assertThatThrownBy(() -> resource.saveAccount(accountDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Current user could not have permission to change settings account");
        }
    }

    @Test
    void rejectsEmailUsedByAnotherUser() {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getAuthenticationTypeUserLogin).thenReturn(java.util.Optional.of(AuthenticationType.FORM_LOGIN));
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(java.util.Optional.of("current"));
            AuthUser existing = user("other");
            when(authUserRepository.findOneByEmailIgnoreCaseAndNotDeleted("used@example.com", AuthenticationType.FORM_LOGIN))
                .thenReturn(java.util.Optional.of(existing));

            assertThatThrownBy(() -> resource.saveAccount(accountDto()))
                .isInstanceOf(EmailAlreadyUsedException.class);
        }
    }

    @Test
    void returnsNoContentForUnknownLanguage() {
        ResponseEntity<Void> response = resource.setLanguageCookie(null, null, "unknown");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void returnsNoContentForKnownLanguage() {
        ResponseEntity<Void> response = resource.setLanguageCookie(null, null, "IT");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void silentlyHandlesMissingPasswordResetUser() {
        when(authUserService.requestPasswordResetByMail("missing@example.com", AuthenticationType.FORM_LOGIN))
            .thenReturn(java.util.Optional.empty());

        resource.requestPasswordReset("missing@example.com");

        verify(authUserService).requestPasswordResetByMail("missing@example.com", AuthenticationType.FORM_LOGIN);
    }

    @Test
    void rejectsExpiredPasswordResetKey() {
        AuthUser user = user("reset-user");
        user.setResetDate(Instant.now().minusSeconds(3600));
        when(authUserRepository.findOneByResetKey("key")).thenReturn(java.util.Optional.of(user));
        ApplicationProperties.Password passwordProperties = new ApplicationProperties.Password();
        when(properties.getPassword()).thenReturn(passwordProperties);
        passwordProperties.setHoursKeyResetPasswordExpired(1);

        assertThatThrownBy(() -> resource.finishPasswordReset(keyAndPassword("key", "NewPassword1!")))
            .isInstanceOf(com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException.class);
    }

    private Jwt jwt() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("group-1"));
        when(jwt.getClaimAsString("sub")).thenReturn("sub-1");
        return jwt;
    }

    private AuthUserAccountDTO accountDto() {
        AuthUserAccountDTO dto = new AuthUserAccountDTO();
        dto.setEmail("used@example.com");
        dto.setFirstName("Current");
        dto.setLastName("User");
        dto.setLangKey("it");
        return dto;
    }

    private AuthUserDTO userDto() {
        AuthUserDTO dto = new AuthUserDTO();
        dto.setLogin("current");
        dto.setEmail("current@example.com");
        return dto;
    }

    private AuthUser user(String login) {
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setLogin(login);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail(login + "@example.com");
        user.setAuthenticationType(AuthenticationType.FORM_LOGIN);
        user.setResetDate(Instant.now());
        return user;
    }

    private com.nexigroup.pagopa.cruscotto.sert.web.rest.vm.KeyAndPasswordVM keyAndPassword(String key, String password) {
        com.nexigroup.pagopa.cruscotto.sert.web.rest.vm.KeyAndPasswordVM vm = new com.nexigroup.pagopa.cruscotto.sert.web.rest.vm.KeyAndPasswordVM();
        vm.setKey(key);
        vm.setNewPassword(password);
        return vm;
    }
}
