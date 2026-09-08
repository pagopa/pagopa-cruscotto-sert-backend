package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvStateValidation;
import io.undertow.util.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SearchInstanceService {
    SearchInstanceDTO create(SearchInstanceDTO dto);
    @Transactional(readOnly = true)
    Page<SearchInstanceDTO> findAll(Pageable pageable);
    @Transactional(readOnly = true)
    Optional<SearchInstanceDTO> findOne(UUID id);
    SearchInstanceDTO update(UUID id, SearchInstanceDTO dto);
    void delete(UUID id);
    Optional<SearchInstanceDTO> performAction(UUID id, SearchInstanceAction action);
    void uploadCsv(UUID id, MultipartFile file);
    void execute(UUID id);
    void rerun(UUID id);
    Optional<byte[]> getLastResult(UUID id);


    Optional<byte[]> downloadPerimeterCsv(UUID instanceId);
}
