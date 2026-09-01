package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchPerimeterFileRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.CsvFromFilterGenerator;
import com.nexigroup.pagopa.cruscotto.sert.service.storage.BlobStorageService;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;

import org.springframework.util.StringUtils;

import  org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
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

    private final CsvFromFilterGenerator csvFromFilterGenerator;

    public SearchInstanceService(
        SearchInstanceRepository instanceRepository,
        SearchPerimeterFileRepository perimeterFileRepository,
        BlobStorageService blobStorageService,
        CsvFromFilterGenerator csvFromFilterGenerator
    ) {
        this.instanceRepository = instanceRepository;
        this.perimeterFileRepository = perimeterFileRepository;
        this.blobStorageService = blobStorageService;
        this.csvFromFilterGenerator = csvFromFilterGenerator;
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

        // If a perimeter filter is provided in the DTO, generate a CSV (NAV:pa_emittente)
        // using CsvFromFilterGenerator and persist it in SEARCH_PERIMETER_FILE.content
        try {
            if (dto.getPerimeterFilter() != null) {
                byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                if (csvBytes != null && csvBytes.length > 0) {
                    String content = new String(csvBytes, StandardCharsets.UTF_8);
                    savePerimeterFileContent(entity, "generated-perimeter.csv", content, "SYSTEM_GENERATED");
                }
            }
        } catch (Exception e) {
            // Log and continue: do not block creation if generation fails
            // Optionally you may choose to throw to fail creation
            // Using System.err for minimal dependency; consider using a logger
            System.err.println("Failed to generate perimeter CSV: " + e.getMessage());
        }

        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<SearchInstanceDTO> findAll(Pageable pageable) {
        Page<SearchInstance> all = instanceRepository.findAll(pageable);
        List<SearchInstanceDTO> collect = all.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageCustomImpl<SearchInstanceDTO>(collect,
            pageable, all==null || all.isEmpty()? 0L: all.getTotalElements());

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

        // If a perimeter filter is provided in the DTO, (re)generate a CSV and persist content
        try {
            if (dto.getPerimeterFilter() != null) {
                byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                if (csvBytes != null && csvBytes.length > 0) {
                    String content = new String(csvBytes, StandardCharsets.UTF_8);
                    savePerimeterFileContent(entity, "generated-perimeter.csv", content, "SYSTEM_GENERATED");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to generate perimeter CSV on update: " + e.getMessage());
        }

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
                .filePath(blobPath)
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
        boolean exists = blobStorageService.exists(file.getFilePath());
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

    private void savePerimeterFileContent(SearchInstance instance, String filename, String content, String source) {
        SearchPerimeterFile perimeterFile = SearchPerimeterFile.builder()
            .id(UUID.randomUUID())
            .instance(instance)
            .source(source)
            .template(null)
            .fileName(filename)
            .filePath(null)
            .rowsCount(countNonEmptyLines(content))
            .validationStatus("PENDING")
            .createdAt(Instant.now())
            .content(content)
            .build();
        perimeterFileRepository.save(perimeterFile);

        instance.setUpdatedAt(Instant.now());
        instanceRepository.save(instance);
    }

    private Long countNonEmptyLines(String content) {
        if (!StringUtils.hasText(content)) return 0L;
        String[] lines = content.split("\\r?\\n");
        long count = 0;
        for (String l : lines) {
            if (StringUtils.hasText(l)) count++;
        }
        return count;
    }
}
