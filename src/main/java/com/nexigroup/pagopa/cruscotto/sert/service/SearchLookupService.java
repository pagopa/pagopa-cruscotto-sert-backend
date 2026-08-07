package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagCanale;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPa;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPsp;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPaEmittente;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPsp;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagStazione;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagCanaleRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagIntermediarioPaRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagIntermediarioPspRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagPaEmittenteRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagPspRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagStazioneRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionTokensRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service providing lookup operations used by SearchLookupResource. Returns paged and sortable results
 * based on the corresponding entities.
 */
@Service
public class SearchLookupService {

    private final AnagPspRepository anagPspRepository;
    private final AnagStazioneRepository anagStazioneRepository;
    private final AnagIntermediarioPaRepository anagIntermediarioPaRepository;
    private final AnagIntermediarioPspRepository anagIntermediarioPspRepository;
    private final AnagCanaleRepository anagCanaleRepository;
    private final AnagPaEmittenteRepository anagPaEmittenteRepository;
    private final PositionTokensRepository positionTokensRepository;

    public SearchLookupService(
        AnagPspRepository anagPspRepository,
        AnagStazioneRepository anagStazioneRepository,
        AnagIntermediarioPaRepository anagIntermediarioPaRepository,
        AnagIntermediarioPspRepository anagIntermediarioPspRepository,
        AnagCanaleRepository anagCanaleRepository,
        AnagPaEmittenteRepository anagPaEmittenteRepository,
        PositionTokensRepository positionTokensRepository
    ) {
        this.anagPspRepository = anagPspRepository;
        this.anagStazioneRepository = anagStazioneRepository;
        this.anagIntermediarioPaRepository = anagIntermediarioPaRepository;
        this.anagIntermediarioPspRepository = anagIntermediarioPspRepository;
        this.anagCanaleRepository = anagCanaleRepository;
        this.anagPaEmittenteRepository = anagPaEmittenteRepository;
        this.positionTokensRepository = positionTokensRepository;
    }

    public Page<AnagPsp> findPsp(Pageable pageable) {
        return anagPspRepository.findAll(pageable);
    }

    public Page<AnagStazione> findStations(Pageable pageable) {
        return anagStazioneRepository.findAll(pageable);
    }

    public Page<AnagIntermediarioPa> findIntermediaries(Pageable pageable) {
        return anagIntermediarioPaRepository.findAll(pageable);
    }

    public Page<AnagIntermediarioPsp> findIntermediariesPsp(Pageable pageable) {
        return anagIntermediarioPspRepository.findAll(pageable);
    }

    public Page<AnagCanale> findChannels(Pageable pageable) {
        return anagCanaleRepository.findAll(pageable);
    }

    public Page<AnagPaEmittente> findPaEmittente(Pageable pageable) {
        return anagPaEmittenteRepository.findAll(pageable);
    }

    public Page<String> findTouchpoints(Pageable pageable) {
        return positionTokensRepository.findDistinctTouchpoints(pageable);
    }

    public Page<String> findPaymentMethods(Pageable pageable) {
        return positionTokensRepository.findDistinctPaymentMethods(pageable);
    }
}
