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
    public Page<AnagPsp> findPsp(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagPspRepository.findAll(pageable);
        }
        return anagPspRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<AnagStazione> findStations(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagStazioneRepository.findAll(pageable);
        }
        return anagStazioneRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<AnagIntermediarioPa> findIntermediaries(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagIntermediarioPaRepository.findAll(pageable);
        }
        return anagIntermediarioPaRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<AnagIntermediarioPsp> findIntermediariesPsp(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagIntermediarioPspRepository.findAll(pageable);
        }
        return anagIntermediarioPspRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<AnagCanale> findChannels(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagCanaleRepository.findAll(pageable);
        }
        return anagCanaleRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<AnagPaEmittente> findPaEmittente(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagPaEmittenteRepository.findAll(pageable);
        }
        return anagPaEmittenteRepository.findAllWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<String> findTouchpoints(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return positionTokensRepository.findDistinctTouchpoints(pageable);
        }
        return positionTokensRepository.findDistinctTouchpointsWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<String> findPaymentMethods(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return positionTokensRepository.findDistinctPaymentMethods(pageable);
        }
        return positionTokensRepository.findDistinctPaymentMethodsWithSearch(search.trim(), pageable);
    }
}
