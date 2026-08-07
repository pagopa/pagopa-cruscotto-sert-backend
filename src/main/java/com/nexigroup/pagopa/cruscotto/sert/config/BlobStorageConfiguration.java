package com.nexigroup.pagopa.cruscotto.sert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

@Configuration
public class BlobStorageConfiguration {

    @Value("${azure.blob.connection-string:}")
    private String connectionString;

    @Value("${azure.blob.container-name:sert-container}")
    private String containerName;

    @Value("${azure.blob.create-if-not-exists:true}")
    private boolean createIfNotExists;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient client = blobServiceClient.getBlobContainerClient(containerName);
        if (createIfNotExists && !client.exists()) {
            client.create();
        }
        return client;
    }
}
