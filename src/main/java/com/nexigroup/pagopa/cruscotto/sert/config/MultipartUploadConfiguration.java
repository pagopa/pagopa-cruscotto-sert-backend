package com.nexigroup.pagopa.cruscotto.sert.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartUploadConfiguration {

    static final long MAX_FILE_SIZE_BYTES = DataSize.ofMegabytes(21).toBytes();
    static final long MAX_REQUEST_SIZE_BYTES = DataSize.ofMegabytes(22).toBytes();

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(MAX_FILE_SIZE_BYTES));
        factory.setMaxRequestSize(DataSize.ofBytes(MAX_REQUEST_SIZE_BYTES));
        return factory.createMultipartConfig();
    }
}