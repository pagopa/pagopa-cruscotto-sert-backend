package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchPerimeterFileRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.storage.BlobStorageService;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service implementation for Search Instance lifecycle. Methods are implemented minimally to support
 * CSV upload integration with Blob Storage and persistence of SEARCH_PERIMETER_FILE metadata.
 */
@Service
@Transactional
public class SearchInstanceService {

    private static final String ENTITY_NAME = "searchInstance";

    private final SearchInstanceRepository instanceRepository;

    private final SearchPerimeterFileRepository perimeterFileRepository;

    private final BlobStorageService blobStorageService;

    public SearchInstanceService(
        SearchInstanceRepository instanceRepository,
        SearchPerimeterFileRepository perimeterFileRepository,
        BlobStorageService blobStorageService
    ) {
        this.instanceRepository = instanceRepository;
        this.perimeterFileRepository = perimeterFileRepository;
        this.blobStorageService = blobStorageService;
    }

    public SearchInstanceDTO create(SearchInstanceDTO dto) {
        SearchInstance entity = SearchInstance.builder()
            .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
            .name(dto.getName())
            .inputType(dto.getInputType())
            .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
            .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now())
            .updatedAt(Instant.now())
            .build();
        instanceRepository.save(entity);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<SearchInstanceDTO> findAll() {
        return instanceRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SearchInstanceDTO> findOne(UUID id) {
        return instanceRepository.findById(id).map(this::toDto);
    }

    public SearchInstanceDTO update(UUID id, SearchInstanceDTO dto) {
        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        entity.setName(dto.getName());
        entity.setInputType(dto.getInputType());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);
        return toDto(entity);
    }

    public void delete(UUID id) {
        if (!instanceRepository.existsById(id)) {
            throw new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound");
        }
        instanceRepository.deleteById(id);
    }

    public SearchInstanceDTO duplicate(UUID id) {
        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        SearchInstance copy = SearchInstance.builder()
            .id(UUID.randomUUID())
            .name(entity.getName() + " (copy)")
            .inputType(entity.getInputType())
            .status("DRAFT")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        instanceRepository.save(copy);
        return toDto(copy);
    }

    public void archive(UUID id) {
        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        entity.setStatus("ARCHIVED");
        entity.setArchivedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);
    }

    public void restore(UUID id) {
        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        entity.setStatus("DRAFT");
        entity.setArchivedAt(null);
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);
    }

    /**
     * Perform lifecycle action: DUPLICATE returns a DTO, ARCHIVE/RESTORE return empty Optional
     */
    public Optional<SearchInstanceDTO> performAction(UUID id, SearchInstanceAction action) {
        switch (action) {
            case DUPLICATE:
                return Optional.ofNullable(duplicate(id));
            case ARCHIVE:
                archive(id);
                return Optional.empty();
            case RESTORE:
                restore(id);
                return Optional.empty();
            default:
                throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    public void uploadCsv(UUID id, MultipartFile file) {
        try {
            SearchInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));

            String fileName = file.getOriginalFilename();
            UUID fileId = UUID.randomUUID();
            String blobPath = String.format("search-instances/%s/perimeter/%s-%s", id, fileId, fileName != null ? fileName : "perimeter.csv");

            String url = blobStorageService.upload(blobPath, file.getInputStream(), file.getSize(), file.getContentType());

            SearchPerimeterFile perimeterFile = SearchPerimeterFile.builder()
                .id(fileId)
                .instance(instance)
                .source("USER_UPLOAD")
                .template(null)
                .fileName(fileName)
                .blobPath(blobPath)
                .rowsCount(null)
                .validationStatus("PENDING")
                .createdAt(Instant.now())
                .build();

            perimeterFileRepository.save(perimeterFile);

            // update instance updatedAt and keep reference to last execution/file if needed
            instance.setUpdatedAt(Instant.now());
            instanceRepository.save(instance);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading CSV file", e);
        }
    }

    public boolean validateCsv(UUID id) {
        SearchInstance instance = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));

        Optional<SearchPerimeterFile> maybe = perimeterFileRepository.findTopByInstanceOrderByCreatedAtDesc(instance);
        if (maybe.isEmpty()) {
            return false;
        }
        SearchPerimeterFile file = maybe.get();
        boolean exists = blobStorageService.exists(file.getBlobPath());
        file.setValidationStatus(exists ? "VALID" : "INVALID");
        perimeterFileRepository.save(file);
        return exists;
    }

    public void execute(UUID id) {
        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        entity.setStatus("READY");
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);
    }

    public void rerun(UUID id) {
        // same behavior for now
        execute(id);
    }

    public Optional<byte[]> getLastResult(UUID id) {
        // For now, try to find SearchResult linked to instance and download the zipBlobPath
        // This code is left as a no-op until SearchResultRepository integration is required.
        return Optional.empty();
    }

    private SearchInstanceDTO toDto(SearchInstance entity) {
        return SearchInstanceDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .inputType(entity.getInputType())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
