package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchFilter;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchExecution;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchExecutionStep;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchResult;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.CustomerGeneratedFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.SearchInstanceStatus;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchFilterRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchExecutionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchExecutionStepRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchPerimeterFileRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchResultRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchExecutionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchExecutionStepDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.CsvFromFilterGenerator;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvStateValidation;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplate;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvValidationResult;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator.MassiveSearchCsvValidator;
import com.nexigroup.pagopa.cruscotto.sert.service.storage.BlobStorageService;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import io.undertow.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Search Instance lifecycle. Methods are implemented minimally to support
 * CSV upload integration with Blob Storage and persistence of SEARCH_PERIMETER_FILE metadata.
 */
@Service
@Transactional
public class SearchInstanceServiceImpl implements SearchInstanceService {

    private static final String ENTITY_NAME = "searchInstance";
    public static final String HEADER_NAV_PA_N = "\"NAV\";\"EC\"\n";

    private final Logger log = LoggerFactory.getLogger(SearchInstanceServiceImpl.class);

    private final SearchInstanceRepository instanceRepository;

    private final SearchExecutionRepository executionRepository;

    private final SearchExecutionStepRepository executionStepRepository;

    private final SearchResultRepository resultRepository;


    private final SearchPerimeterFileRepository perimeterFileRepository;

    private final BlobStorageService blobStorageService;

    private final CsvFromFilterGenerator csvFromFilterGenerator;

    private final SearchFilterRepository searchFilterRepository;

    private final MassiveSearchCsvValidator csvValidator;
    private final ObjectMapper objectMapper ;

    public SearchInstanceServiceImpl(
        SearchInstanceRepository instanceRepository,
        SearchExecutionRepository executionRepository,
        SearchExecutionStepRepository executionStepRepository,
        SearchResultRepository resultRepository,
        SearchPerimeterFileRepository perimeterFileRepository,
        BlobStorageService blobStorageService,
        CsvFromFilterGenerator csvFromFilterGenerator,
        SearchFilterRepository searchFilterRepository, MassiveSearchCsvValidator csvValidator,
        ObjectMapper objectMapper
    ) {
        this.instanceRepository = instanceRepository;
        this.executionRepository = executionRepository;
        this.executionStepRepository = executionStepRepository;
        this.resultRepository = resultRepository;
        this.perimeterFileRepository = perimeterFileRepository;
        this.blobStorageService = blobStorageService;
        this.csvFromFilterGenerator = csvFromFilterGenerator;
        this.searchFilterRepository = searchFilterRepository;
        this.csvValidator = csvValidator;
        this.objectMapper= objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public SearchInstanceDTO create(SearchInstanceDTO dto) {

        SearchInstance entity = SearchInstance.builder()
            .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
            .name(dto.getName())
            .inputType(dto.getInputType().name())
            .selectedReports(dto.getSelectedReports())
            .status(dto.getStatus() != null ? dto.getStatus().name() : "DRAFT")
            .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now())
            .updatedAt(Instant.now())
            .build();

        instanceRepository.save(entity);
        // If a perimeter filter is provided in the DTO, generate a CSV (NAV:pa_emittente)
        // using CsvFromFilterGenerator and persist it in SEARCH_PERIMETER_FILE.content (upsert)
        try {
            if (dto.getPerimeterFilter() != null) {

                searchFilterRepository.save(SearchFilter.builder()
                    .instanceId(entity.getId())
                    .createdAt((dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now()))
                    .updatedAt((dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now()))
                    .filterJson(objectMapper.writeValueAsString(dto.getPerimeterFilter()))
                    .build());
                //byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                //if (csvBytes != null && csvBytes.length > 0) {
                //    String content = HEADER_NAV_PA_N +new String(csvBytes, StandardCharsets.UTF_8);
                //    upsertPerimeterFileContent(entity, "generated-perimeter.csv", content, CustomerGeneratedFile.GENERATED_FROM_FILTERS.name());
                //}
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
    public Page<SearchExecutionDTO> findExecutions(UUID instanceId, Pageable pageable) {
        return executionRepository.findByInstance_Id(instanceId, pageable).map(this::toExecutionDto);
    }

    @Transactional(readOnly = true)
    public Page<SearchExecutionStepDTO> findExecutionSteps(UUID executionId, Pageable pageable) {
        return executionStepRepository.findByExecutionId(executionId, pageable).map(this::toExecutionStepDto);
    }

    @Transactional(readOnly = true)
    public Optional<SearchResultDTO> findResult(UUID instanceId) {
        return resultRepository.findById(instanceId).map(this::toResultDto);
    }

    private SearchExecutionDTO toExecutionDto(SearchExecution execution) {
        return SearchExecutionDTO.builder()
            .id(execution.getId())
            .instanceId(execution.getInstance().getId())
            .status(execution.getStatus())
            .startedAt(execution.getStartedAt())
            .completedAt(execution.getCompletedAt())
            .totalInputRows(execution.getTotalInputRows())
            .processedRows(execution.getProcessedRows())
            .generatedFiles(execution.getGeneratedFiles())
            .errorCode(execution.getErrorCode())
            .errorMessage(execution.getErrorMessage())
            .createdAt(execution.getCreatedAt())
            .updatedAt(execution.getUpdatedAt())
            .build();
    }

    private SearchExecutionStepDTO toExecutionStepDto(SearchExecutionStep step) {
        return SearchExecutionStepDTO.builder()
            .id(step.getId())
            .executionId(step.getExecutionId())
            .instanceId(step.getInstanceId())
            .phase(step.getPhase())
            .attemptNo(step.getAttemptNo())
            .status(step.getStatus())
            .windowFrom(step.getWindowFrom())
            .windowTo(step.getWindowTo())
            .rowsProcessed(step.getRowsProcessed())
            .startedAt(step.getStartedAt())
            .endedAt(step.getEndedAt())
            .durationMs(step.getDurationMs())
            .errorCode(step.getErrorCode())
            .errorMessage(step.getErrorMessage())
            .createdAt(step.getCreatedAt())
            .build();
    }

    private SearchResultDTO toResultDto(SearchResult result) {
        return SearchResultDTO.builder()
            .instanceId(result.getId())
            .executionId(result.getExecutionId())
            .zipFileName(result.getZipFileName())
            .zipFilePath(result.getZipFilePath())
            .zipSizeBytes(result.getZipSizeBytes())
            .positionRows(result.getPositionRows())
            .attemptRows(result.getAttemptRows())
            .transferRows(result.getTransferRows())
            .generatedAt(result.getGeneratedAt())
            .updatedAt(result.getUpdatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public Optional<SearchInstanceDTO> findOne(UUID id) {
        return instanceRepository.findById(id).map(this::toDto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public SearchInstanceDTO update(UUID id, SearchInstanceDTO dto) {

        SearchInstance entity = instanceRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("SearchInstance not found", ENTITY_NAME, "idnotfound"));
        entity.setName(dto.getName());
        entity.setInputType(dto.getInputType().name());
        entity.setSelectedReports(dto.getSelectedReports());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus().name());
        }
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);

        // If a perimeter filter is provided in the DTO, (re)generate a CSV and persist/overwrite content
        //todo se il perimeter non cambia , non creare un nuovo csv
        try {
            if (dto.getPerimeterFilter() != null) {

                searchFilterRepository.save(SearchFilter.builder()
                    .instanceId(entity.getId())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(( Instant.now()))
                    .filterJson(objectMapper.writeValueAsString(dto.getPerimeterFilter()))
                    .build());

                //byte[] csvBytes = csvFromFilterGenerator.generateCsv(dto.getPerimeterFilter());
                //if (csvBytes != null && csvBytes.length > 0) {
                //    String content = HEADER_NAV_PA_N + new String(csvBytes, StandardCharsets.UTF_8);
                //    upsertPerimeterFileContent(entity, "generated-perimeter.csv", content, CustomerGeneratedFile.GENERATED_FROM_FILTERS.name());
                //}
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
            .selectedReports(entity.getSelectedReports())
            .status(SearchInstanceStatus.DRAFT.name())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        instanceRepository.save(copy);

        // If original has a stored SearchFilter, deserialize and generate CSV for the new copy
        Optional<SearchFilter> maybeFilter = searchFilterRepository.findById(entity.getId());

        maybeFilter.ifPresent(sf -> {
            if (sf.getFilterJson() != null && !sf.getFilterJson().isEmpty()) {
                try {
                    SearchBulkFilterDTO filterDto = objectMapper.readValue(
                        sf.getFilterJson(),
                        SearchBulkFilterDTO.class
                    );

                    //if (filterDto != null) {
                    //    byte[] csvBytes = csvFromFilterGenerator.generateCsv(filterDto);

                    //    if (csvBytes != null && csvBytes.length > 0) {
                    //        String content = HEADER_NAV_PA_N
                    //            + new String(csvBytes, StandardCharsets.UTF_8);

                    //        upsertPerimeterFileContent(
                    //            copy,
                    //            "generated-perimeter.csv",
                    //            content,
                    //            CustomerGeneratedFile.GENERATED_FROM_FILTERS.name()
                    //        );
                    //    }
                    //}
                } catch (Exception ex) {
                    log.error(
                        "Failed to deserialize SearchFilter.filterJson for instance {}: {}",
                        entity.getId(),
                        ex.getMessage(),
                        ex
                    );
                }
            }
        });

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void uploadCsv(UUID id, MultipartFile file)  {
        try {
            SearchInstance instance = instanceRepository.findById(id)
                .orElseThrow(() ->
                    new BadRequestAlertException(
                        "SearchInstance not found",
                        ENTITY_NAME,
                        "idnotfound"
                    )
                );

            String fileName = file.getOriginalFilename();
            UUID fileId = UUID.randomUUID();

            String blobPath = String.format(
                "search-instances/%s/perimeter/%s-%s",
                id,
                fileId,
                fileName != null ? fileName : "perimeter.csv"
            );

            // Leggo il file una sola volta
            byte[] fileBytes = file.getBytes();

            // Creo un nuovo InputStream per il validator
            CsvTemplate csvTemplate;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                csvTemplate = csvValidator.extractCsvTemplate(is);
            }

            SearchPerimeterFile perimeterFile =
                perimeterFileRepository.findByInstance(instance);
            String content = new String(fileBytes, StandardCharsets.UTF_8);
            if (perimeterFile == null) {
                perimeterFile = SearchPerimeterFile.builder()
                    .id(fileId)
                    .instance(instance)
                    .source(CustomerGeneratedFile.USER_UPLOADED.name())
                    .template(csvTemplate.name())
                    .fileName(fileName)
                    .filePath(blobPath)
                    .rowsCount(countNonEmptyLines(content))
                    .validationStatus(CsvStateValidation.VALID.name())
                    .createdAt(Instant.now())
                    .content(content)
                    .build();
            } else {
                perimeterFile.setTemplate(csvTemplate.name());
                perimeterFile.setFileName(fileName);
                perimeterFile.setValidationStatus(CustomerGeneratedFile.USER_UPLOADED.name());
                perimeterFile.setCreatedAt(Instant.now());
                perimeterFile.setValidationStatus(CsvStateValidation.VALID.name());
                perimeterFile.setRowsCount(countNonEmptyLines(content));
                perimeterFile.setContent(content);
            }

            perimeterFileRepository.save(perimeterFile);

            instance.setUpdatedAt(Instant.now());
            instanceRepository.save(instance);

        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to read uploaded file",
                e
            );
        }
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
        return resultRepository.findById(id)
            .map(result -> buildBlobPath(result.getZipFilePath(), result.getZipFileName()))
            .flatMap(blobStorageService::download);
    }

    private String buildBlobPath(String zipFilePath, String zipFileName) {
        if (zipFilePath.endsWith("/")) {
            return zipFilePath + zipFileName;
        }
        return zipFilePath + "/" + zipFileName;
    }

    private SearchInstanceDTO toDto(SearchInstance entity) {
        SearchFilter searchFilter = searchFilterRepository.findById(entity.getId())
            .orElse(null);
        SearchBulkFilterDTO searchBulkFilterDTO= null;
        if (searchFilter!=null ){
            try {
                searchBulkFilterDTO = objectMapper.readValue(searchFilter.getFilterJson(), SearchBulkFilterDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        return SearchInstanceDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .inputType(PerimeterSearchType.fromString(entity.getInputType()))
            .selectedReports(entity.getSelectedReports())
            .status(SearchInstanceStatus.fromString(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .perimeterFilter(searchBulkFilterDTO)
            .build();


    }


    private void upsertPerimeterFileContent(SearchInstance instance, String filename, String content, String source) {
        SearchPerimeterFile perimeterFile = SearchPerimeterFile.builder()
            .id(UUID.randomUUID())
            .instance(instance)
            .source(source)
            .template(CsvTemplate.NAV_PA.name())
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

    @Override
    public Optional<byte[]> downloadPerimeterCsv(UUID instanceId) {
        SearchInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new BadRequestAlertException(
                "SearchInstance not found",
                "searchInstance",
                "idnotfound"
            ));

        return perimeterFileRepository
            .findTopByInstanceOrderByCreatedAtDesc(instance)
            .filter(file -> StringUtils.hasText(file.getContent()))
            .map(file -> file.getContent().getBytes(StandardCharsets.UTF_8));
    }
}
