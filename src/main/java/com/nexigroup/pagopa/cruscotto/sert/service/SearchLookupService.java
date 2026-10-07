package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchLookupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SearchLookupService {
    Page<SearchLookupDTO> findPsp(String search, Pageable pageable);

    Page<SearchLookupDTO> findStations(String search, Pageable pageable);

    Page<SearchLookupDTO> findIntermediaries(String search, Pageable pageable);

    Page<SearchLookupDTO> findIntermediariesPsp(String search, Pageable pageable);

    Page<SearchLookupDTO> findChannels(String search, Pageable pageable);

    Page<SearchLookupDTO> findPaEmittente(String search, Pageable pageable);

    Page<SearchLookupDTO> findTouchpoints(String search, Pageable pageable);

    Page<SearchLookupDTO> findPaymentMethods(String search, Pageable pageable);

    Optional<SearchLookupDTO> findPspById(Long id);

    Optional<SearchLookupDTO> findStationsById(Long id);

    Optional<SearchLookupDTO> findIntermediaryById(Long id);

    Optional<SearchLookupDTO> findIntermediaryPspById(Long id);

    Optional<SearchLookupDTO> findChannelById(Long id);

    Optional<SearchLookupDTO> findPaEmittenteById(Long id);

    Optional<SearchLookupDTO> findTouchpointById(Long id);

    Optional<SearchLookupDTO> findPaymentMethodById(Long id);

    Optional<SearchLookupDTO> findTouchpointByCode(String code);

    Optional<SearchLookupDTO> findPaymentMethodByCode(String code);
}
