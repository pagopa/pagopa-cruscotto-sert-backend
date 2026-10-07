package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.security.util.PasswordExpiredUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class PasswordExpiryPolicyTest {

    @Test
    void passwordExpiresOnTheConfiguredBoundaryDate() {
        ZonedDateTime changedAt = LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault());

        assertThat(PasswordExpiredUtils.isPasswordNonExpired(changedAt, 30)).isFalse();
        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(changedAt, 30)).isEqualTo(LocalDate.now());
    }

    @Test
    void missingExpiryConfigurationDoesNotExpirePassword() {
        ZonedDateTime changedAt = LocalDate.now().minusYears(10).atStartOfDay(ZoneId.systemDefault());

        assertThat(PasswordExpiredUtils.isPasswordNonExpired(changedAt, null)).isTrue();
        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(changedAt, null)).isNull();
        assertThat(PasswordExpiredUtils.isPasswordNonExpired(null, 30)).isTrue();
        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(null, 30)).isNull();
    }
}