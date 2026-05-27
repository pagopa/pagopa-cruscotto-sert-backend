package com.nexigroup.pagopa.cruscotto.sert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsClientConfig {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String awsRegion;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(() -> software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey))
            .httpClientBuilder(
                ApacheHttpClient.builder()
                    .socketTimeout(java.time.Duration.ofSeconds(30))
                    .connectionTimeout(java.time.Duration.ofSeconds(10))
            )
            .overrideConfiguration(
                ClientOverrideConfiguration.builder().addExecutionInterceptor(new AwsRequestLoggingInterceptor()).build()
            )
            .build();
    }
}
