package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import com.nexigroup.pagopa.cruscotto.sert.repository.*;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchLookupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    private final AnagTouchpointRepository anagTouchpointRepository;
    private final AnagPaymentMethodRepository anagPaymentMethodRepository;
    private final PositionTokensRepository positionTokensRepository;

    public SearchLookupServiceImpl(
        AnagPspRepository anagPspRepository,
        AnagStazioneRepository anagStazioneRepository,
        AnagIntermediarioPaRepository anagIntermediarioPaRepository,
        AnagIntermediarioPspRepository anagIntermediarioPspRepository,
        AnagCanaleRepository anagCanaleRepository,
        AnagPaEmittenteRepository anagPaEmittenteRepository,
        AnagTouchpointRepository anagTouchpointRepository,
        AnagPaymentMethodRepository anagPaymentMethodRepository,
        PositionTokensRepository positionTokensRepository
    ) {
        this.anagPspRepository = anagPspRepository;
        this.anagStazioneRepository = anagStazioneRepository;
        this.anagIntermediarioPaRepository = anagIntermediarioPaRepository;
        this.anagIntermediarioPspRepository = anagIntermediarioPspRepository;
        this.anagCanaleRepository = anagCanaleRepository;
        this.anagPaEmittenteRepository = anagPaEmittenteRepository;
        this.anagTouchpointRepository = anagTouchpointRepository;
        this.anagPaymentMethodRepository = anagPaymentMethodRepository;
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
            return anagTouchpointRepository.findAllCodes(pageable);
        }
        return anagTouchpointRepository.findAllCodesWithSearch(search.trim(), pageable);
    }

    @Override
    public Page<String> findPaymentMethods(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return anagPaymentMethodRepository.findAllCodes(pageable);
        }
        return anagPaymentMethodRepository.findAllCodesWithSearch(search.trim(), pageable);
    }

    @Override
    public Optional<SearchLookupDTO> findPspById(Long id) {
        return toShortId(id).flatMap(anagPspRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), entity.getDescription()));
    }

    @Override
    public Optional<SearchLookupDTO> findStationsById(Long id) {
        return toShortId(id).flatMap(anagStazioneRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    @Override
    public Optional<SearchLookupDTO> findIntermediaryById(Long id) {
        return toShortId(id).flatMap(anagIntermediarioPaRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), entity.getDescription()));
    }

    @Override
    public Optional<SearchLookupDTO> findIntermediaryPspById(Long id) {
        return toShortId(id).flatMap(anagIntermediarioPspRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), entity.getDescription()));
    }

    @Override
    public Optional<SearchLookupDTO> findChannelById(Long id) {
        return toShortId(id).flatMap(anagCanaleRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    @Override
    public Optional<SearchLookupDTO> findPaEmittenteById(Long id) {
        return anagPaEmittenteRepository.findById(id).map(entity -> toDto(entity.getId(), entity.getCodice(), entity.getDescription()));
    }

    @Override
    public Optional<SearchLookupDTO> findTouchpointById(Long id) {
        return toShortId(id).flatMap(anagTouchpointRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    @Override
    public Optional<SearchLookupDTO> findPaymentMethodById(Long id) {
        return toShortId(id).flatMap(anagPaymentMethodRepository::findById).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    @Override
    public Optional<SearchLookupDTO> findTouchpointByCode(String code) {
        return anagTouchpointRepository.findByCodice(code).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    @Override
    public Optional<SearchLookupDTO> findPaymentMethodByCode(String code) {
        return anagPaymentMethodRepository.findByCodice(code).map(entity -> toDto(entity.getId().longValue(), entity.getCodice(), null));
    }

    private Optional<Short> toShortId(Long id) {
        if (id < Short.MIN_VALUE || id > Short.MAX_VALUE) {
            return Optional.empty();
        }
        return Optional.of(id.shortValue());
    }

    private SearchLookupDTO toDto(Long id, String codice, String description) {
        return SearchLookupDTO.builder().id(id).codice(codice).description(description).build();
    }
}
