package com.nexigroup.pagopa.cruscotto.sert.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class AzureBlobStorageServiceTest {

    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=test;";
    private static final String CONTAINER_NAME = "test-container";
    private static final String BLOB_PATH = "folder/blob.txt";

    @Test
    void createsClientFromConfiguredConnectionStringAndContainer() {
        BlobServiceClient serviceClient = mock(BlobServiceClient.class);
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        when(serviceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(containerClient);

        try (MockedConstruction<BlobServiceClientBuilder> builder = mockConstruction(
            BlobServiceClientBuilder.class,
            (mock, context) -> {
                when(mock.connectionString(CONNECTION_STRING)).thenReturn(mock);
                when(mock.buildClient()).thenReturn(serviceClient);
            }
        )) {
            AzureBlobStorageService service = new AzureBlobStorageService(CONNECTION_STRING, CONTAINER_NAME);

            assertThat(service).isNotNull();
            assertThat(builder.constructed()).hasSize(1);
            verify(serviceClient).getBlobContainerClient(CONTAINER_NAME);
        }
    }

    @Test
    void rejectsMissingConfigurationProperties() {
        assertThatThrownBy(() -> new AzureBlobStorageService(null, CONTAINER_NAME))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("azure.blob.connection-string property is required");
        assertThatThrownBy(() -> new AzureBlobStorageService(CONNECTION_STRING, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("azure.blob.container-name property is required");
    }

    @Test
    void uploadsBlobAndSetsContentType() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.getBlobUrl()).thenReturn("https://storage.example/folder/blob.txt");
        AzureBlobStorageService service = createService(blobClient);
        InputStream data = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));

        String url = service.upload(BLOB_PATH, data, 7, "text/plain");

        assertThat(url).isEqualTo("https://storage.example/folder/blob.txt");
        verify(blobClient).upload(data, 7L, true);
        verify(blobClient).setHttpHeaders(argThat(headers -> "text/plain".equals(headers.getContentType())));
        verify(blobClient).getBlobUrl();
    }

    @Test
    void uploadsBlobWithoutSettingHeadersWhenContentTypeIsNull() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.getBlobUrl()).thenReturn("https://storage.example/folder/blob.txt");
        AzureBlobStorageService service = createService(blobClient);
        InputStream data = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));

        service.upload(BLOB_PATH, data, 7, null);

        verify(blobClient).upload(data, 7L, true);
        verify(blobClient, never()).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void downloadsExistingBlobAsBytes() throws IOException {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(true);
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(0);
            output.write("content".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(blobClient).download(any(OutputStream.class));
        AzureBlobStorageService service = createService(blobClient);

        Optional<byte[]> result = service.download(BLOB_PATH);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow()).isEqualTo("content".getBytes(StandardCharsets.UTF_8));
        verify(blobClient).exists();
        verify(blobClient).download(any(OutputStream.class));
    }

    @Test
    void returnsEmptyWhenBlobDoesNotExist() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(false);
        AzureBlobStorageService service = createService(blobClient);

        Optional<byte[]> result = service.download(BLOB_PATH);

        assertThat(result).isEmpty();
        verify(blobClient).exists();
        verify(blobClient, never()).download(any(OutputStream.class));
    }

    @Test
    void returnsEmptyWhenBlobDownloadFails() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(true);
        doThrow(new IllegalStateException("download failed"))
            .when(blobClient).download(any(OutputStream.class));
        AzureBlobStorageService service = createService(blobClient);

        Optional<byte[]> result = service.download(BLOB_PATH);

        assertThat(result).isEmpty();
        verify(blobClient).exists();
        verify(blobClient).download(any(OutputStream.class));
    }

    @Test
    void deletesExistingBlob() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(true);
        AzureBlobStorageService service = createService(blobClient);

        service.delete(BLOB_PATH);

        verify(blobClient).exists();
        verify(blobClient).delete();
    }

    @Test
    void doesNotDeleteMissingBlob() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(false);
        AzureBlobStorageService service = createService(blobClient);

        service.delete(BLOB_PATH);

        verify(blobClient).exists();
        verify(blobClient, never()).delete();
    }

    @Test
    void reportsBlobExistence() {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(true);
        AzureBlobStorageService service = createService(blobClient);

        assertThat(service.exists(BLOB_PATH)).isTrue();
        verify(blobClient).exists();
    }

    private AzureBlobStorageService createService(BlobClient blobClient) {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        when(containerClient.getBlobClient(BLOB_PATH)).thenReturn(blobClient);
        when(containerClient.getBlobContainerName()).thenReturn(CONTAINER_NAME);
        BlobServiceClient serviceClient = mock(BlobServiceClient.class);
        when(serviceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(containerClient);

        try (MockedConstruction<BlobServiceClientBuilder> ignored = mockConstruction(
            BlobServiceClientBuilder.class,
            (mock, context) -> {
                when(mock.connectionString(CONNECTION_STRING)).thenReturn(mock);
                when(mock.buildClient()).thenReturn(serviceClient);
            }
        )) {
            return new AzureBlobStorageService(CONNECTION_STRING, CONTAINER_NAME);
        }
    }
}
