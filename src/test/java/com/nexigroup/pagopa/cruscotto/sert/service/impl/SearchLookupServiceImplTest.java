package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPsp;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagCanaleRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagIntermediarioPaRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagIntermediarioPspRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagPaEmittenteRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagPaymentMethodRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagPspRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagStazioneRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AnagTouchpointRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionTokensRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class SearchLookupServiceImplTest {

    private final AnagPspRepository pspRepository = mock(AnagPspRepository.class);
    private final AnagStazioneRepository stationRepository = mock(AnagStazioneRepository.class);
    private final AnagIntermediarioPaRepository intermediaryPaRepository = mock(AnagIntermediarioPaRepository.class);
    private final AnagIntermediarioPspRepository intermediaryPspRepository = mock(AnagIntermediarioPspRepository.class);
    private final AnagCanaleRepository channelRepository = mock(AnagCanaleRepository.class);
    private final AnagPaEmittenteRepository paRepository = mock(AnagPaEmittenteRepository.class);
    private final AnagTouchpointRepository touchpointRepository = mock(AnagTouchpointRepository.class);
    private final AnagPaymentMethodRepository paymentMethodRepository = mock(AnagPaymentMethodRepository.class);
    private final PositionTokensRepository positionTokensRepository = mock(PositionTokensRepository.class);
    private final SearchLookupServiceImpl service = new SearchLookupServiceImpl(
        pspRepository,
        stationRepository,
        intermediaryPaRepository,
        intermediaryPspRepository,
        channelRepository,
        paRepository,
        touchpointRepository,
        paymentMethodRepository,
        positionTokensRepository
    );

    @Test
    void delegatesBlankAndTrimmedPspSearchesToCorrectRepositoryMethods() {
        var pageable = PageRequest.of(0, 10);
        when(pspRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of()));
        when(pspRepository.findAllWithSearch("bank", pageable)).thenReturn(new PageImpl<>(java.util.List.of()));

        service.findPsp("   ", pageable);
        service.findPsp("  bank  ", pageable);

        verify(pspRepository).findAll(pageable);
        verify(pspRepository).findAllWithSearch("bank", pageable);
    }

    @Test
    void mapsPspByIdAndSkipsIdsOutsideShortRange() {
        when(pspRepository.findById((short) 12)).thenReturn(Optional.of(
            AnagPsp.builder().id((short) 12).codice("PSP12").description("Payment provider").build()
        ));

        var result = service.findPspById(12L);

        assertThat(result).get().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(12L);
            assertThat(dto.getCodice()).isEqualTo("PSP12");
            assertThat(dto.getDescription()).isEqualTo("Payment provider");
        });
        assertThat(service.findPspById(Short.MAX_VALUE + 1L)).isEmpty();
        verifyNoInteractions(stationRepository, intermediaryPaRepository, intermediaryPspRepository,
            channelRepository, paRepository, touchpointRepository, paymentMethodRepository, positionTokensRepository);
    }
}