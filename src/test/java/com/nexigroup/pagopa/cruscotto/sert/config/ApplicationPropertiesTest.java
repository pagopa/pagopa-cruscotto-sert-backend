package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationPropertiesTest {

    @Test
    void exposesConfigurableSecurityAndPasswordProperties() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.setEnableCsrf(true);
        properties.getPassword().setDayPasswordExpired(90);
        properties.getPassword().setFailedLoginAttempts(5);
        properties.getPassword().setHoursKeyResetPasswordExpired(24);
        properties.getAuthGroup().setSuperAdmin("admins");

        assertThat(properties.isEnableCsrf()).isTrue();
        assertThat(properties.getPassword().getDayPasswordExpired()).isEqualTo(90);
        assertThat(properties.getPassword().getFailedLoginAttempts()).isEqualTo(5);
        assertThat(properties.getPassword().getHoursKeyResetPasswordExpired()).isEqualTo(24);
        assertThat(properties.getAuthGroup().getSuperAdmin()).isEqualTo("admins");
    }
}