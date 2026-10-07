package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class DomainUserAuthenticationBehaviorTest {

    private final AuthUserRepository userRepository = mock(AuthUserRepository.class);
    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final DomainUserDetailsService service = new DomainUserDetailsService(userRepository, permissionRepository);

    @Test
    void loadsActiveUserWithItsGroupAuthority() {
        AuthUser user = user("active", true, false, ZonedDateTime.now());
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("active", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("active");

        assertThat(details.getUsername()).isEqualTo("active");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("ROLE_OPERATOR");
    }

    @Test
    void returnsExpiredPasswordAuthorityAndHonorsBlockedState() {
        AuthUser user = user("expired", true, true, ZonedDateTime.now().minusYears(2));
        user.setPasswordExpiredDay(1);
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("expired", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("expired");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities())
            .extracting(authority -> authority.getAuthority())
            .containsExactly(Constants.ROLE_PASSWORD_EXPIRED);
    }

    @Test
    void rejectsMissingOrInactiveUser() {
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("missing", AuthenticationType.FORM_LOGIN)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("missing")).isInstanceOf(UsernameNotFoundException.class);

        AuthUser inactive = user("inactive", false, false, null);
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("inactive", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.of(inactive));
        assertThatThrownBy(() -> service.loadUserByUsername("inactive")).isInstanceOf(UserNotActivatedException.class);
    }

    @Test
    void mapsGroupPermissionsToAuthorities() {
        AuthUser user = user("active", true, false, null);
        AuthPermission permission = new AuthPermission().modulo("SERT").nome("SEARCH");
        when(permissionRepository.findAllPermissionsByGroupId(12L)).thenReturn(List.of(permission));
        user.getGroup().setId(12L);

        assertThat(service.getAuthorities(user)).extracting(authority -> authority.getAuthority()).containsExactly("SERT.SEARCH");
    }

    private AuthUser user(String login, boolean activated, boolean blocked, ZonedDateTime passwordChanged) {
        AuthGroup group = new AuthGroup().nome("ROLE_OPERATOR");
        AuthUser user = new AuthUser();
        user.setLogin(login);
        user.setPassword("encoded-password");
        user.setActivated(activated);
        user.setBlocked(blocked);
        user.setLastPasswordChangeDate(passwordChanged);
        user.setPasswordExpiredDay(90);
        user.setGroup(group);
        return user;
    }
}