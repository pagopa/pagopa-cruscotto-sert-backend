package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchExecutionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchExecutionStepDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvStateValidation;
import io.undertow.util.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SearchInstanceService {
    SearchInstanceDTO create(SearchInstanceDTO dto);
    @Transactional(readOnly = true)
    Page<SearchInstanceDTO> findAll(String search, LocalDate createdFrom, LocalDate createdTo, Pageable pageable);
    @Transactional(readOnly = true)
    Page<SearchExecutionDTO> findExecutions(UUID instanceId, Pageable pageable);
    @Transactional(readOnly = true)
    Page<SearchExecutionStepDTO> findExecutionSteps(UUID executionId, Pageable pageable);
    @Transactional(readOnly = true)
    Optional<SearchResultDTO> findResult(UUID instanceId);
    @Transactional(readOnly = true)
    Optional<SearchInstanceDTO> findOne(UUID id);
    SearchInstanceDTO update(UUID id, SearchInstanceDTO dto);
    void delete(UUID id);
    Optional<SearchInstanceDTO> performAction(UUID id, SearchInstanceAction action);
    void  uploadCsv(UUID id, MultipartFile file);
    void execute(UUID id);
    void rerun(UUID id);
    Optional<byte[]> getLastResult(UUID id);


    Optional<byte[]> downloadPerimeterCsv(UUID instanceId);
}
