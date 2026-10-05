package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CorsFilter;
import tech.jhipster.config.JHipsterProperties;

class WebConfigurerTest {

    @Test
    void startupAndCorsFilterAreConfigured() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        WebConfigurer configurer = new WebConfigurer(environment, new JHipsterProperties());

        configurer.onStartup(mock(ServletContext.class));

        assertThat(configurer.corsFilter()).isInstanceOf(CorsFilter.class);
    }
}