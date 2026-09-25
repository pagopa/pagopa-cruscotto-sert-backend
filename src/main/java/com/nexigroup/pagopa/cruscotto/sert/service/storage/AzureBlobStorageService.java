package com.nexigroup.pagopa.cruscotto.sert.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Service
public class AzureBlobStorageService implements BlobStorageService {

    private final Logger log = LoggerFactory.getLogger(AzureBlobStorageService.class);

    private final BlobContainerClient containerClient;


    public AzureBlobStorageService(
        @Value("${azure.blob.connection-string}") String connectionString,
        @Value("${azure.blob.container-name}") String containerName
    ) {
        Objects.requireNonNull(connectionString, "azure.blob.connection-string property is required");
        Objects.requireNonNull(containerName, "azure.blob.container-name property is required");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public String upload(String blobPath, InputStream data, long length, String contentType) {
        log.debug("Uploading blob '{}' ({} bytes) to container {}", blobPath, length, containerClient.getBlobContainerName());
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        blobClient.upload(data, length, true);
        if (contentType != null) {
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.setHttpHeaders(headers);
        }
        String url = blobClient.getBlobUrl();
        log.debug("Uploaded blob available at {}", url);
        return url;
    }

    @Override
    public Optional<byte[]> download(String blobPath) {
        log.debug("Downloading blob '{}' from container {}", blobPath, containerClient.getBlobContainerName());
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        if (!blobClient.exists()) {
            log.debug("Blob '{}' does not exist", blobPath);
            return Optional.empty();
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            blobClient.download(os);
            return Optional.of(os.toByteArray());
        } catch (Exception e) {
            log.error("Error while downloading blob {}: {}", blobPath, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String blobPath) {
        log.debug("Deleting blob '{}' from container {}", blobPath, containerClient.getBlobContainerName());
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        if (blobClient.exists()) {
            blobClient.delete();
        }
    }

    @Override
    public boolean exists(String blobPath) {
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        return blobClient.exists();
    }
}
