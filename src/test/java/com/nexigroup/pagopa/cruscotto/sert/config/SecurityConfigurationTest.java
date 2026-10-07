package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void createsPasswordEncoderAndMvcMatcherBuilder() {
        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        String encodedPassword = passwordEncoder.encode("test-password");

        assertThat(passwordEncoder.matches("test-password", encodedPassword)).isTrue();
        assertThat(configuration.mvc(new HandlerMappingIntrospector())).isInstanceOf(MvcRequestMatcher.Builder.class);
    }
}