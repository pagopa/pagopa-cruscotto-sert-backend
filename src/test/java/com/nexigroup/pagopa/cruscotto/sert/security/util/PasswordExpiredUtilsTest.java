package com.nexigroup.pagopa.cruscotto.sert.security.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class PasswordExpiredUtilsTest {

    @Test
    void computesExpirationDateAndValidityBoundary() {
        ZonedDateTime changedAt = LocalDate.now().minusDays(10).atStartOfDay(ZoneOffset.UTC);

        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(changedAt, 10)).isEqualTo(LocalDate.now());
        assertThat(PasswordExpiredUtils.isPasswordNonExpired(changedAt, 10)).isFalse();
        assertThat(PasswordExpiredUtils.isPasswordNonExpired(changedAt, 11)).isTrue();
    }

    @Test
    void treatsMissingExpiryConfigurationOrChangeDateAsNotExpired() {
        assertThat(PasswordExpiredUtils.isPasswordNonExpired(null, 10)).isTrue();
        assertThat(PasswordExpiredUtils.isPasswordNonExpired(ZonedDateTime.now(), null)).isTrue();
        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(null, 10)).isNull();
        assertThat(PasswordExpiredUtils.getPasswordExpiredDate(ZonedDateTime.now(), null)).isNull();
    }
}