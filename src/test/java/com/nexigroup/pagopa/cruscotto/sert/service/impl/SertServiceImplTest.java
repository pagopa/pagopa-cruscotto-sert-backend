package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.repository.PositionRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class SertServiceImplTest {

    private final PositionRepository positionRepository = mock(PositionRepository.class);
    private final SertServiceImpl service = new SertServiceImpl(positionRepository);

    @Test
    void skipsEmptyTokenSearchAndMapsNavSearchRows() {
        var pageable = PageRequest.of(0, 10);
        assertThat(service.searchByToken("pa", "nav", "   ", pageable)).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(positionRepository);

        when(positionRepository.findByNavOrPa("nav-1", "pa-1", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(new Object[] { "nav-1", "pa-1" }), pageable, 1));

        var result = service.searchByNav("nav-1", "pa-1", pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("nav-1");
            assertThat(item.getPaEmittente()).isEqualTo("pa-1");
        });
    }

    @Test
    void searchesExtraInfoSortsAndPagesMappedRows() {
        var pageable = PageRequest.of(0, 1, Sort.by("nav"));
        when(positionRepository.findGroupedByExtraValueAndOptionalNavAndPa("reason", null, null)).thenReturn(List.<Object[]>of(
            new Object[] { "NAV-B", "Beta", "b, c", "PA-B" },
            new Object[] { "NAV-A", "Alpha", " a,  b ", "PA-A" }
        ));

        var result = service.searchExtra(null, null, "reason", pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-A");
            assertThat(item.getPaEmittenteDsc()).isEqualTo("Alpha");
            assertThat(item.getPaEmittente()).isEqualTo("PA-A");
            assertThat(item.getMatch()).containsExactly("a", "b");
        });
        verify(positionRepository).findGroupedByExtraValueAndOptionalNavAndPa("reason", null, null);
    }

    @Test
    void returnsEmptyForNullExtraSearchAndMapsTokenInfoPaymentFields() {
        var pageable = PageRequest.of(0, 10);
        assertThat(service.searchExtra(null, null, null, pageable)).isEmpty();

        Object[] row = new Object[22];
        row[0] = "351234567890123456";
        row[1] = "PA-1";
        row[2] = Instant.parse("2026-01-01T00:00:00Z");
        row[3] = "IUV-1";
        row[4] = "CRID-1";
        row[5] = "61626364";
        row[6] = java.sql.Date.valueOf("2026-01-02");
        row[7] = Instant.parse("2026-01-03T00:00:00Z");
        row[8] = "ok";
        row[9] = new java.math.BigDecimal("12.50");
        row[10] = 1.5;
        row[18] = "cart-1";
        when(positionRepository.findTokenDetailRow("61626364")).thenReturn(List.<Object[]>of(row));
        when(positionRepository.countOkTokensByNavAndPa("351234567890123456", "PA-1")).thenReturn(2L);

        var token = service.getTokenInfo(" 61626364 ");

        assertThat(token.getIsPayedToken()).isTrue();
        assertThat(token.getPositionInfo().getNav()).isEqualTo("351234567890123456");
        assertThat(token.getPositionInfo().getIuv()).isEqualTo("IUV-1");
        assertThat(token.getPayed().getToken()).isEqualTo("abcd");
        assertThat(token.getPayed().getMultiOutcome()).isTrue();
        assertThat(token.getAmount().getAmount()).isEqualTo(12.5);
        assertThat(token.getPaymentInfo().getIsCart()).isTrue();
        assertThat(token.getPaymentInfo().getIsDw()).isTrue();
        assertThat(service.getTokenInfo(" ")).isNull();
    }
}