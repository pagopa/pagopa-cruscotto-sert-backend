package com.nexigroup.pagopa.cruscotto.sert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchFilter;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchFilterRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchPerimeterFileRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.CsvFromFilterGenerator;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvStateValidation;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.storage.BlobStorageService;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;

import org.springframework.util.StringUtils;

import  org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final String GENERATED_FROM_FILTERS = "GENERATED_FROM_FILTERS";

    private final Logger log = LoggerFactory.getLogger(SearchInstanceService.class);

    private final SearchInstanceRepository instanceRepository;

    private final SearchPerimeterFileRepository perimeterFileRepository;

    private final BlobStorageService blobStorageService;

    private final CsvFromFilterGenerator csvFromFilterGenerator;

    private final SearchFilterRepository searchFilterRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchInstanceService(
        SearchInstanceRepository instanceRepository,
        SearchPerimeterFileRepository perimeterFileRepository,
        BlobStorageService blobStorageService,
        CsvFromFilterGenerator csvFromFilterGenerator,
        SearchFilterRepository searchFilterRepository
    ) {
        this.instanceRepository = instanceRepository;
        this.perimeterFileRepository = perimeterFileRepository;
        this.blobStorageService = blobStorageService;
        this.csvFromFilterGenerator = csvFromFilterGenerator;
        this.searchFilterRepository = searchFilterRepository;
    }

    public SearchInstanceDTO create(SearchInstanceDTO dto) {
        SearchInstance entity = SearchInstance.builder()
            .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
            .name(dto.getName())
            .inputType(dto.getInputType().name())
            .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
            .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now())
            .updatedAt(Instant.now())
            .build();

        instanceRepository.save(entity);
        // If a perimeter filter is provided in the DTO, generate a CSV (NAV:pa_emittente)
        // using CsvFromFilterGenerator and persist it in SEARCH_PERIMETER_FILE.content (upsert)
        try {
            if (dto.getPerimeterFilter() != null) {
                byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                if (csvBytes != null && csvBytes.length > 0) {
                    String content = new String(csvBytes, StandardCharsets.UTF_8);
                    upsertPerimeterFileContent(entity, "generated-perimeter.csv", content, GENERATED_FROM_FILTERS);
                }
            }
        } catch (Exception e) {
            // Log and continue: do not block creation if generation fails
            log.error("Failed to generate perimeter CSV on create for instance {}: {}", entity.getId(), e.getMessage(), e);
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
        entity.setInputType(dto.getInputType().name());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);

        // If a perimeter filter is provided in the DTO, (re)generate a CSV and persist/overwrite content
        try {
            if (dto.getPerimeterFilter() != null) {
                byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                if (csvBytes != null && csvBytes.length > 0) {
                    String content = new String(csvBytes, StandardCharsets.UTF_8);
                    upsertPerimeterFileContent(entity, "generated-perimeter.csv", content, "GENERATED_FROM_FILTERS");
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate perimeter CSV on update for instance {}: {}", entity.getId(), e.getMessage(), e);
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

        // If original has a stored SearchFilter, deserialize and generate CSV for the new copy
        try {
            Optional<SearchFilter> maybeFilter = searchFilterRepository.findById(entity.getId());
            if (maybeFilter.isPresent()) {
                SearchFilter sf = maybeFilter.get();
                if (sf.getFilterJson() != null && !sf.getFilterJson().isEmpty()) {
                    try {
                        SearchBulkFilterDTO filterDto = objectMapper.readValue(sf.getFilterJson(), SearchBulkFilterDTO.class);
                        if (filterDto != null) {
                            byte[] csvBytes = csvFromFilterGenerator.generateCsv(filterDto);
                            if (csvBytes != null && csvBytes.length > 0) {
                                String content = new String(csvBytes, StandardCharsets.UTF_8);
                                upsertPerimeterFileContent(copy, "generated-perimeter.csv", content, "GENERATED_FROM_FILTERS");
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Failed to deserialize SearchFilter.filterJson for instance {}: {}", entity.getId(), ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate perimeter CSV for duplicate instance {}: {}", copy.getId(), e.getMessage(), e);
        }

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
            .inputType(PerimeterSearchType.fromString(entity.getInputType()))
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private void upsertPerimeterFileContent(SearchInstance instance, String filename, String content, String source) {
        SearchPerimeterFile perimeterFile = SearchPerimeterFile.builder()
            .id(UUID.randomUUID())
            .instance(instance)
            .source(source)
            .template(null)
            .fileName(filename)
            .filePath(null)
            .rowsCount(countNonEmptyLines(content))
            .validationStatus(CsvStateValidation.VALID.name())
            .createdAt(Instant.now())
            .content(content)
            .build();
        perimeterFileRepository.save(perimeterFile);

        instance.setUpdatedAt(Instant.now());
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
