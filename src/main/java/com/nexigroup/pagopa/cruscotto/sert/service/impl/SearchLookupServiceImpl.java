package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import com.nexigroup.pagopa.cruscotto.sert.repository.*;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service providing lookup operations used by SearchLookupResource. Returns paged and sortable results
 * based on the corresponding entities.
 */
@Service
public class SearchLookupServiceImpl implements SearchLookupService {

    private final AnagPspRepository anagPspRepository;
    private final AnagStazioneRepository anagStazioneRepository;
    private final AnagIntermediarioPaRepository anagIntermediarioPaRepository;
    private final AnagIntermediarioPspRepository anagIntermediarioPspRepository;
    private final AnagCanaleRepository anagCanaleRepository;
    private final AnagPaEmittenteRepository anagPaEmittenteRepository;
    private final PositionTokensRepository positionTokensRepository;

    public SearchLookupServiceImpl(
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

    @Override
    public Page<AnagPsp> findPsp(Pageable pageable) {
        return anagPspRepository.findAll(pageable);
    }

    @Override
    public Page<AnagStazione> findStations(Pageable pageable) {
        return anagStazioneRepository.findAll(pageable);
    }

    @Override
    public Page<AnagIntermediarioPa> findIntermediaries(Pageable pageable) {
        return anagIntermediarioPaRepository.findAll(pageable);
    }

    @Override
    public Page<AnagIntermediarioPsp> findIntermediariesPsp(Pageable pageable) {
        return anagIntermediarioPspRepository.findAll(pageable);
    }

    @Override
    public Page<AnagCanale> findChannels(Pageable pageable) {
        return anagCanaleRepository.findAll(pageable);
    }

    @Override
    public Page<AnagPaEmittente> findPaEmittente(Pageable pageable) {
        return anagPaEmittenteRepository.findAll(pageable);
    }

    @Override
    public Page<String> findTouchpoints(Pageable pageable) {
        return positionTokensRepository.findDistinctTouchpoints(pageable);
    }

    @Override
    public Page<String> findPaymentMethods(Pageable pageable) {
        return positionTokensRepository.findDistinctPaymentMethods(pageable);
    }
}
