package com.nexigroup.pagopa.cruscotto.sert.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserUtilsTest {

    private final AuthUserRepository userRepository = mock(AuthUserRepository.class);
    private final UserUtils userUtils = new UserUtils(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findsLoggedUserUsingJwtSubject() {
        AuthUser user = new AuthUser();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").issuedAt(Instant.now()).subject("subject-1").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(userRepository.findOneBySub("subject-1")).thenReturn(Optional.of(user));

        assertThat(userUtils.getLoggedUser()).isSameAs(user);
        verify(userRepository).findOneBySub("subject-1");
    }

    @Test
    void failsWhenJwtSubjectHasNoUser() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").issuedAt(Instant.now()).subject("unknown").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(userRepository.findOneBySub("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(userUtils::getLoggedUser)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Current user login not found");
    }
}