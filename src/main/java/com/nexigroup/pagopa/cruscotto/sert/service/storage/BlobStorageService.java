package com.nexigroup.pagopa.cruscotto.sert.service.storage;

import java.io.InputStream;
import java.util.Optional;

public interface BlobStorageService {

    /**
     * Uploads a blob and returns the accessible URL (may be internal URL depending on storage)
     */
    String upload(String blobPath, InputStream data, long length, String contentType);

    /**
     * Downloads a blob as byte array if present
     */
    Optional<byte[]> download(String blobPath);

    /**
     * Deletes a blob if present
     */
    void delete(String blobPath);

    /**
     * Checks whether a blob exists
     */
    boolean exists(String blobPath);
}
