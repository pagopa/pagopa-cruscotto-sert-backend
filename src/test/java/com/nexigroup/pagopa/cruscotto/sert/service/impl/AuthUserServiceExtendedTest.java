package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserHistoryRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserCreateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthUserServiceExtendedTest {

    private final AuthUserRepository userRepository = mock(AuthUserRepository.class);
    private final AuthUserHistoryRepository historyRepository = mock(AuthUserHistoryRepository.class);
    private final AuthGroupRepository groupRepository = mock(AuthGroupRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthUserService service = new AuthUserService(
        userRepository,
        historyRepository,
        new ApplicationProperties(),
        mock(QueryBuilder.class),
        groupRepository,
        mock(AuthPermissionRepository.class),
        mock(CacheManager.class),
        passwordEncoder
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updatesProfileAndRecordsNewPasswordOnlyWhenProvided() {
        AuthUser user = user("updated", 6L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        AuthGroup group = new AuthGroup();
        group.setId(9L);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("encoded-new");

        var result = service.updateUser(updateRequest(4L, "NewPassword1!"));

        assertThat(result).isPresent();
        assertThat(user.getEmail()).isEqualTo("user@example.org");
        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getPasswordExpiredDay()).isZero();
        assertThat(user.isBlocked()).isFalse();
        assertThat(user.getGroup()).isSameAs(group);
        verify(historyRepository).save(any());

        AuthUser noPasswordChange = user("no-password", 2L);
        when(userRepository.findById(6L)).thenReturn(Optional.of(noPasswordChange));
        service.updateUser(updateRequest(6L, " "));
        assertThat(noPasswordChange.getPassword()).isEqualTo("existing-hash");
        verify(historyRepository, org.mockito.Mockito.times(1)).save(any());
        assertThat(service.updateUser(updateRequest(5L, null))).isEmpty();
    }

    @Test
    void synchronizesJwtUserAndBuildsUserDtoFromClaims() {
        Jwt jwt = Jwt.withTokenValue("jwt-token")
            .header("alg", "none")
            .issuedAt(Instant.parse("2026-10-01T10:00:00Z"))
            .claim("sub", "subject-1")
            .claim("oid", "oid-1")
            .claim("preferred_username", "USER@example.org")
            .claim("name", "Jane Doe (NEXI)")
            .claim("roles", List.of("group-object"))
            .build();
        AuthGroup group = new AuthGroup();
        group.setNome("ROLE_USER");
        group.setLivelloVisibilita(1);
        when(userRepository.findOneBySub("subject-1")).thenReturn(Optional.empty());
        when(groupRepository.findOneByObjectId(List.of("group-object"))).thenReturn(List.of(group));
        when(userRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.buildFromJwt(jwt, Set.of("SERT.SEARCH"));

        assertThat(dto.getLogin()).isEqualTo("USER@example.org");
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getGroupName()).isEqualTo("ROLE_USER");
        assertThat(dto.getAuthorities()).containsExactly("SERT.SEARCH");
        assertThat(dto.getAuthenticationType()).isEqualTo(AuthenticationType.ENTRA_ID);

        AuthUser existing = user("old", 7L);
        when(userRepository.findOneBySub("subject-1")).thenReturn(Optional.of(existing));
        AuthUser synced = service.syncUserFromJwt(jwt);
        assertThat(synced).isSameAs(existing);
        assertThat(synced.getLastLoginAt()).isNotNull();
        assertThat(synced.getOid()).isEqualTo("oid-1");
    }

    @Test
    void findsUsersAndAuthoritiesWhenPresentAndReturnsEmptyWhenMissing() {
        AuthUser user = user("reader", 8L);
        when(userRepository.findOneByLoginAndNotDeleted("reader", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThat(service.getUserById(8L)).isPresent();
        assertThat(service.getUserById(9L)).isEmpty();
        assertThat(service.getUserByLogin("reader")).isPresent();
        assertThat(service.findUserByLogin("reader")).containsSame(user);
        assertThat(service.getAuthUserWithAuthoritiesByLogin("reader")).isPresent();
        when(userRepository.findOneByLoginAndNotDeleted("missing", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.empty());
        assertThat(service.getAuthUserWithAuthoritiesByLogin("missing")).isEmpty();
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.argThat(candidate -> candidate.getLogin().equals("missing")));
    }

    @Test
    void createsFormUserWithDefaultLanguageAndPasswordHistory() {
        setCurrentJwtPrincipal("admin@example.org");
        AuthUser loggedUser = user("admin@example.org", 1L);
        when(userRepository.findOneByLoginAndNotDeleted("admin@example.org", AuthenticationType.ENTRA_ID))
            .thenReturn(Optional.of(loggedUser));
        when(passwordEncoder.encode("InitialPassword1!")).thenReturn("encoded-initial");
        when(userRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthUserCreateRequestBean request = new AuthUserCreateRequestBean();
        request.setLogin("new-user");
        request.setFirstName("First");
        request.setLastName("Last");
        request.setEmail("NEW@EXAMPLE.ORG");
        request.setPassword("InitialPassword1!");
        request.setGroupId(4L);

        AuthUser created = service.createUser(request);

        assertThat(created.getEmail()).isEqualTo("new@example.org");
        assertThat(created.getLangKey()).isEqualTo("it");
        assertThat(created.getPassword()).isEqualTo("encoded-initial");
        assertThat(created.getActivated()).isTrue();
        assertThat(created.getAuthenticationType()).isEqualTo(AuthenticationType.FORM_LOGIN);
        assertThat(created.getGroup()).isNull();
        verify(userRepository).save(created);
        verify(historyRepository).save(any());
    }

    @Test
    void changesPasswordAfterCheckingCurrentCredentialAndRecordsHistory() {
        setCurrentJwtPrincipal("reader@example.org");
        AuthUser user = user("reader@example.org", 8L);
        user.setPassword("encoded-old");
        when(userRepository.findOneByLoginAndNotDeleted("reader@example.org", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword1!", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("encoded-replacement");

        service.changePassword("OldPassword1!", "NewPassword1!");

        assertThat(user.getPassword()).isEqualTo("encoded-replacement");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastPasswordChangeDate()).isNotNull();
        verify(userRepository).save(user);
        verify(historyRepository).save(any());
    }

    private void setCurrentJwtPrincipal(String username) {
        Jwt jwt = Jwt.withTokenValue("access")
            .header("alg", "none")
            .issuedAt(Instant.now())
            .claim("sub", username)
            .claim("preferred_username", username)
            .claim("roles", List.of("ROLE_ADMIN"))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private AuthUser user(String login, Long id) {
        AuthUser user = new AuthUser();
        user.setId(id);
        user.setLogin(login);
        user.setEmail("OLD@EXAMPLE.ORG");
        user.setPassword("existing-hash");
        user.setFailedLoginAttempts(5);
        user.setBlocked(true);
        user.setGroup(new AuthGroup());
        return user;
    }

    private AuthUserUpdateRequestBean updateRequest(Long id, String password) {
        AuthUserUpdateRequestBean request = new AuthUserUpdateRequestBean();
        request.setId(id);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("USER@EXAMPLE.ORG");
        request.setLangKey("en");
        request.setGroupId(9L);
        request.setPassword(password);
        return request;
    }
}