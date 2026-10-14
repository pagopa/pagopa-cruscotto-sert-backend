package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.MultipartConfigElement;
import org.junit.jupiter.api.Test;

class MultipartUploadConfigurationTest {

    @Test
    void configuresTwentyOneMegabytesPerFileAndTwentyTwoPerRequest() {
        MultipartConfigElement multipartConfig = new MultipartUploadConfiguration().multipartConfigElement();

        assertThat(multipartConfig.getMaxFileSize()).isEqualTo(21L * 1024 * 1024);
        assertThat(multipartConfig.getMaxRequestSize()).isEqualTo(22L * 1024 * 1024);
    }
}