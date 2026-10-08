package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserCreateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.MailService;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PasswordValidator;
import com.nexigroup.pagopa.cruscotto.sert.service.validation.UserResourcePermissionValidator;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.EmailAlreadyUsedException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.LoginAlreadyUsedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthUserResourceTest {

    private final AuthUserService service = mock(AuthUserService.class);
    private final AuthUserRepository repository = mock(AuthUserRepository.class);
    private final PasswordValidator passwordValidator = mock(PasswordValidator.class);
    private final MailService mailService = mock(MailService.class);
    private final UserResourcePermissionValidator permissionValidator = mock(UserResourcePermissionValidator.class);
    private final AuthUserResource resource = new AuthUserResource(service, repository, mailService, passwordValidator, permissionValidator);

    @Test
    void createsUserWhenLoginAndEmailAreAvailable() throws Exception {
        AuthUserCreateRequestBean request = createRequest();
        AuthUser created = user("new-user");
        when(repository.findOneByLoginIgnoreCaseAndNotDeleted("new-user", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.empty());
        when(repository.findOneByEmailIgnoreCaseAndNotDeleted("new@example.com", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.empty());
        when(service.createUser(request)).thenReturn(created);

        ResponseEntity<AuthUser> response = resource.createAuthUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(created);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/auth-users/new-user");
    }

    @Test
    void rejectsDuplicateLogin() {
        AuthUserCreateRequestBean request = createRequest();
        when(repository.findOneByLoginIgnoreCaseAndNotDeleted("new-user", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user("new-user")));

        assertThatThrownBy(() -> resource.createAuthUser(request))
            .isInstanceOf(LoginAlreadyUsedException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        AuthUserCreateRequestBean request = createRequest();
        when(repository.findOneByLoginIgnoreCaseAndNotDeleted("new-user", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.empty());
        when(repository.findOneByEmailIgnoreCaseAndNotDeleted("new@example.com", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user("other")));

        assertThatThrownBy(() -> resource.createAuthUser(request))
            .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void rejectsUpdateForLoggedUser() {
        AuthUserUpdateRequestBean request = updateRequest(1L);
        request.setEmail("current@example.com");
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("current"));
            when(permissionValidator.userCanAccessIdUserResource("current", 1L)).thenReturn(false);
            when(repository.findOneByEmailIgnoreCaseAndNotDeleted("current@example.com", AuthenticationType.FORM_LOGIN))
                .thenReturn(Optional.of(user("current")));

            assertThatThrownBy(() -> resource.updateAuthUser(request))
                .isInstanceOf(BadRequestAlertException.class)
                .satisfies(exception -> assertThat(
                    ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
                ).isEqualTo("L'utente selezionato coincide con l'utente loggato"));
        }
    }

    @Test
    void rejectsUpdateWhenEmailBelongsToAnotherUser() {
        AuthUserUpdateRequestBean request = updateRequest(2L);
        request.setEmail("other@example.com");
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("current"));
            when(permissionValidator.userCanAccessIdUserResource("current", 2L)).thenReturn(false);
            when(repository.findOneByEmailIgnoreCaseAndNotDeleted("other@example.com", AuthenticationType.FORM_LOGIN))
                .thenReturn(Optional.of(user("other")));

            assertThatThrownBy(() -> resource.updateAuthUser(request))
                .isInstanceOf(EmailAlreadyUsedException.class);
        }
    }

    @Test
    void changesUserState() {
        AuthUserDTO user = userDto(1L, "user");
        user.setAuthenticationType(AuthenticationType.FORM_LOGIN);
        user.setActivated(true);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("admin"));
            when(permissionValidator.userCanAccessIdUserResource("admin", 1L)).thenReturn(false);
            when(service.getUserById(1L)).thenReturn(Optional.of(user));

            ResponseEntity<Void> response = resource.changeState(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).changeState(1L, false);
        }
    }

    @Test
    void sendsResetPasswordMail() {
        AuthUser user = user("target");
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("admin"));
            when(permissionValidator.userCanAccessUserResource("admin", "target")).thenReturn(false);
            when(service.requestPasswordResetByLogin("target")).thenReturn(Optional.of(user));

            ResponseEntity<Void> response = resource.resetPasswordUser("target");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(mailService).sendPasswordResetMail(user);
        }
    }

    @Test
    void retrievesUsersWithPagination() {
        PageImpl<AuthUserDTO> page = new PageImpl<>(List.of(userDto(1L, "user")));
        when(service.getAllManagedUsers(Pageable.unpaged())).thenReturn(page);

        ResponseEntity<List<AuthUserDTO>> response = resource.getAllAuthUsers(Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void deletesUser() {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("admin"));
            when(permissionValidator.userCanAccessUserResource("admin", "target")).thenReturn(false);
            when(service.deleteUser("target")).thenReturn(true);

            ResponseEntity<Void> response = resource.deleteAuthUser("target");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(service).deleteUser("target");
        }
    }

    private AuthUserCreateRequestBean createRequest() {
        AuthUserCreateRequestBean request = new AuthUserCreateRequestBean();
        request.setLogin("new-user");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("new@example.com");
        request.setPassword("Password1!");
        request.setGroupId(1L);
        return request;
    }

    private AuthUserUpdateRequestBean updateRequest(Long id) {
        AuthUserUpdateRequestBean request = new AuthUserUpdateRequestBean();
        request.setId(id);
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setEmail("current@example.com");
        request.setGroupId(1L);
        return request;
    }

    private AuthUser user(String login) {
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setLogin(login);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail(login + "@example.com");
        user.setAuthenticationType(AuthenticationType.FORM_LOGIN);
        return user;
    }

    private AuthUserDTO userDto(Long id, String login) {
        AuthUserDTO dto = new AuthUserDTO();
        dto.setId(id);
        dto.setLogin(login);
        dto.setEmail(login + "@example.com");
        return dto;
    }
}
