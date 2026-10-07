package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tech.jhipster.config.JHipsterProperties;

class LoggingConfigurationTest {

    @Test
    void initializesWithoutOptionalJsonOrLogstashAppenders() {
        JHipsterProperties properties = new JHipsterProperties();
        properties.getLogging().setUseJsonFormat(false);
        properties.getLogging().getLogstash().setEnabled(false);

        assertThatCode(() -> new LoggingConfiguration("sert", "8080", properties, new ObjectMapper()))
            .doesNotThrowAnyException();
    }
}