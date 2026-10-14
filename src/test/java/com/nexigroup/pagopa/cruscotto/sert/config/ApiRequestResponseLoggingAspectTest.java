package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiRequestResponseLoggingAspectTest {

    @Test
    void formatsElapsedTimeAsSecondsBelowOneMinute() {
        assertThat(ApiRequestResponseLoggingAspect.formatElapsedTime(2_165)).isEqualTo("2.165");
    }

    @Test
    void formatsElapsedTimeAsMinutesAndSecondsFromOneMinute() {
        assertThat(ApiRequestResponseLoggingAspect.formatElapsedTime(62_165)).isEqualTo("1:02,165");
        assertThat(ApiRequestResponseLoggingAspect.formatElapsedTime(60_000)).isEqualTo("1:00,000");
    }
}