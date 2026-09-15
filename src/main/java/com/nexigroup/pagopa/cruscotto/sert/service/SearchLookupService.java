package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchLookupService {
    Page<AnagPsp> findPsp(Pageable pageable);

    Page<AnagStazione> findStations(Pageable pageable);

    Page<AnagIntermediarioPa> findIntermediaries(Pageable pageable);

    Page<AnagIntermediarioPsp> findIntermediariesPsp(Pageable pageable);

    Page<AnagCanale> findChannels(Pageable pageable);

    Page<AnagPaEmittente> findPaEmittente(Pageable pageable);

    Page<String> findTouchpoints(Pageable pageable);

    Page<String> findPaymentMethods(Pageable pageable);
}
