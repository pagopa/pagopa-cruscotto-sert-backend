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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class DomainUserDetailsServiceTest {

    private final AuthUserRepository userRepository = mock(AuthUserRepository.class);
    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final DomainUserDetailsService service = new DomainUserDetailsService(userRepository, permissionRepository);

    @Test
    void rejectsUnknownAndInactiveUsers() {
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("missing", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User missing was not found");

        AuthUser inactive = user("inactive", false, false, null);
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("inactive", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.of(inactive));
        assertThatThrownBy(() -> service.loadUserByUsername("inactive"))
            .isInstanceOf(UserNotActivatedException.class)
            .hasMessage("User inactive was not activated");
    }

    @Test
    void loadsActiveUserWithGroupAuthorityAndLockState() {
        AuthUser authUser = user("active", true, true, null);
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("active", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.of(authUser));

        UserDetails details = service.loadUserByUsername("active");

        assertThat(details.getUsername()).isEqualTo("active");
        assertThat(details.getPassword()).isEqualTo("password-hash");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("ROLE_OPERATOR");
    }

    @Test
    void assignsPasswordExpiredRoleInsteadOfGroupWhenPasswordHasExpired() {
        AuthUser authUser = user("expired", true, false, ZonedDateTime.now().minusDays(30));
        authUser.setPasswordExpiredDay(10);
        when(userRepository.findOneByLoginIgnoreCaseAndNotDeleted("expired", AuthenticationType.FORM_LOGIN))
            .thenReturn(Optional.of(authUser));

        UserDetails details = service.loadUserByUsername("expired");

        assertThat(details.isCredentialsNonExpired()).isFalse();
        assertThat(details.getAuthorities()).extracting(authority -> authority.getAuthority())
            .containsExactly(Constants.ROLE_PASSWORD_EXPIRED);
    }

    @Test
    void mapsGroupPermissionsToAuthorities() {
        AuthUser authUser = user("active", true, false, null);
        AuthPermission permission = new AuthPermission().modulo("SERT").nome("SEARCH");
        when(permissionRepository.findAllPermissionsByGroupId(12L)).thenReturn(List.of(permission));

        assertThat(service.getAuthorities(authUser))
            .extracting(authority -> authority.getAuthority())
            .containsExactly("SERT.SEARCH");
    }

    private AuthUser user(String login, boolean activated, boolean blocked, ZonedDateTime lastPasswordChange) {
        AuthGroup group = new AuthGroup().nome("ROLE_OPERATOR");
        group.setId(12L);
        AuthUser user = new AuthUser();
        user.setLogin(login);
        user.setPassword("password-hash");
        user.setActivated(activated);
        user.setBlocked(blocked);
        user.setGroup(group);
        user.setLastPasswordChangeDate(lastPasswordChange);
        user.setPasswordExpiredDay(90);
        return user;
    }
}