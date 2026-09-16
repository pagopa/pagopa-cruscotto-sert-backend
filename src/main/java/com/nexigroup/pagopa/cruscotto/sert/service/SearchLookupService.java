package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchLookupService {
    Page<AnagPsp> findPsp(String search, Pageable pageable);

    Page<AnagStazione> findStations(String search, Pageable pageable);

    Page<AnagIntermediarioPa> findIntermediaries(String search, Pageable pageable);

    Page<AnagIntermediarioPsp> findIntermediariesPsp(String search, Pageable pageable);

    Page<AnagCanale> findChannels(String search, Pageable pageable);

    Page<AnagPaEmittente> findPaEmittente(String search, Pageable pageable);

    Page<String> findTouchpoints(String search, Pageable pageable);

    Page<String> findPaymentMethods(String search, Pageable pageable);
}
