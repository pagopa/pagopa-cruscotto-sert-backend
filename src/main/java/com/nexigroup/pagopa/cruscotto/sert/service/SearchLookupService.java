package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchLookupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SearchLookupService {
    Page<AnagPsp> findPsp(String search, Pageable pageable);

    Page<AnagStazione> findStations(String search, Pageable pageable);

    Page<AnagIntermediarioPa> findIntermediaries(String search, Pageable pageable);

    Page<AnagIntermediarioPsp> findIntermediariesPsp(String search, Pageable pageable);

    Page<AnagCanale> findChannels(String search, Pageable pageable);

    Page<AnagPaEmittente> findPaEmittente(String search, Pageable pageable);

    Page<String> findTouchpoints(String search, Pageable pageable);

    Page<String> findPaymentMethods(String search, Pageable pageable);

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
