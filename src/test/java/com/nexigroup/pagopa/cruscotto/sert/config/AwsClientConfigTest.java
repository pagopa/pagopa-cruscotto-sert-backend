package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;

class AwsClientConfigTest {

    @Test
    void createsSesClientFromConfiguredCredentialsAndRegion() {
        AwsClientConfig configuration = new AwsClientConfig();
        ReflectionTestUtils.setField(configuration, "accessKey", "test-access-key");
        ReflectionTestUtils.setField(configuration, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(configuration, "awsRegion", "eu-west-1");

        try (SesClient client = configuration.sesClient()) {
            assertThat(client).isNotNull();
        }
    }
}