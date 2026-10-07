package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserHistoryRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthUserServiceBehaviorTest {

    private final AuthUserRepository userRepository = mock(AuthUserRepository.class);
    private final AuthUserHistoryRepository historyRepository = mock(AuthUserHistoryRepository.class);
    private final AuthGroupRepository groupRepository = mock(AuthGroupRepository.class);
    private final ApplicationProperties properties = new ApplicationProperties();
    private final AuthUserService service = new AuthUserService(
        userRepository,
        historyRepository,
        properties,
        mock(QueryBuilder.class),
        groupRepository,
        mock(AuthPermissionRepository.class),
        mock(CacheManager.class),
        mock(PasswordEncoder.class)
    );

    @Test
    void activatesRegistrationAndClearsActivationKey() {
        AuthUser user = new AuthUser();
        user.setActivationKey("activation");
        when(userRepository.findOneByActivationKey("activation")).thenReturn(Optional.of(user));

        assertThat(service.activateRegistration("activation")).containsSame(user);
        assertThat(user.getActivated()).isTrue();
        assertThat(user.getActivationKey()).isNull();
        when(userRepository.findOneByActivationKey("missing")).thenReturn(Optional.empty());
        assertThat(service.activateRegistration("missing")).isEmpty();
    }

    @Test
    void resetsLoginFailureStateOnlyForActivatedUser() {
        AuthUser user = new AuthUser();
        user.setActivated(true);
        user.setFailedLoginAttempts(4);
        user.setBlocked(true);
        when(userRepository.findOneByLoginAndNotDeleted("active", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user));

        assertThat(service.requestPasswordResetByLogin("active")).containsSame(user);
        assertThat(user.getResetKey()).isNotBlank();
        assertThat(user.getResetDate()).isNotNull();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.isBlocked()).isFalse();

        AuthUser inactive = new AuthUser();
        inactive.setActivated(false);
        when(userRepository.findOneByLoginAndNotDeleted("inactive", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(inactive));
        assertThat(service.requestPasswordResetByLogin("inactive")).isEmpty();
        assertThat(inactive.getResetKey()).isNull();
    }

    @Test
    void logicallyDeletesUserAndTracksFailedAttempts() {
        AuthUser user = new AuthUser();
        user.setLogin("user");
        user.setActivated(true);
        user.setFailedLoginAttempts(2);
        properties.getPassword().setFailedLoginAttempts(2);
        when(userRepository.findOneByLoginAndNotDeleted("user", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user));

        assertThat(service.deleteUser("user")).isTrue();
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedDate()).isNotNull();
        assertThat(user.getActivated()).isFalse();
        verify(userRepository).save(user);

        service.increaseFailedLoginAttempts("user");
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.isBlocked()).isTrue();
        service.resetFailedLoginAttempts("user");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(service.deleteUser("missing")).isFalse();
    }
}