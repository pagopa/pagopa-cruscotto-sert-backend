package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.format.support.DefaultFormattingConversionService;

class DateTimeFormatConfigurationTest {

    @Test
    void registersIsoDateFormatters() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        new DateTimeFormatConfiguration().addFormatters(conversionService);

        assertThat(conversionService.convert("2026-10-05", LocalDate.class)).isEqualTo(LocalDate.of(2026, 10, 5));
    }
}