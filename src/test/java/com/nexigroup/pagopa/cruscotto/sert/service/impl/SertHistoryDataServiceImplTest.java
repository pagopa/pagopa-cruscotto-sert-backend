package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.repository.history.HistoryPositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.online.SertOnlineRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class SertHistoryDataServiceImplTest {

    private final HistoryPositionRepository historyRepository = mock(HistoryPositionRepository.class);
    private final SertOnlineRepository onlineRepository = mock(SertOnlineRepository.class);
    private final SertHistoryDataServiceImpl service = new SertHistoryDataServiceImpl(historyRepository, onlineRepository);

    @Test
    void searchesHistoryByIuvAndIncludesOnlinePositionAndTokenIds() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByIuvAndOptionalNavAndPa("IUV-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.findByIuvAndOptionalNavAndPa(
            "IUV-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        )).thenReturn(new PageImpl<>(List.<Object[]>of(new Object[] { "NAV-1", "PA-1" }), pageable, 1));

        var result = service.searchByIuv("PA-1", "NAV-1", "IUV-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-1");
            assertThat(item.getPaEmittente()).isEqualTo("PA-1");
        });
        verify(onlineRepository).findByIuvAndOptionalNavAndPa("IUV-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).findByIuvAndOptionalNavAndPa(
            "IUV-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        );
    }

    @Test
    void searchesHistoryByIuvWithoutOnlineIdsWhenOnlineSearchHasNoResults() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByIuvAndOptionalNavAndPa("IUV-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.findByIuvAndOptionalNavAndPa(
            "IUV-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        ))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.searchByIuv(null, null, "IUV-1", dateFrom, dateTo, pageable);

        assertThat(result).isEmpty();
        verify(historyRepository).findByIuvAndOptionalNavAndPa(
            "IUV-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        );
    }

    @Test
    void searchesHistoryByCartAndIncludesOnlinePositionAndTokenIds() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByCartAndOptionalNavAndPa("CART-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.findByCartAndOptionalNavAndPa(
            "CART-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        )).thenReturn(new PageImpl<>(List.<Object[]>of(new Object[] { "NAV-1", "PA-1" }), pageable, 1));

        var result = service.searchByCart("PA-1", "NAV-1", "CART-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-1");
            assertThat(item.getPaEmittente()).isEqualTo("PA-1");
        });
        verify(onlineRepository).findByCartAndOptionalNavAndPa("CART-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).findByCartAndOptionalNavAndPa(
            "CART-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        );
    }

    @Test
    void searchesHistoryByCartWhenOnlineSearchHasNoResults() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByCartAndOptionalNavAndPa("CART-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.findByCartAndOptionalNavAndPa(
            "CART-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        ))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.searchByCart(null, null, "CART-1", dateFrom, dateTo, pageable);

        assertThat(result).isEmpty();
        verify(historyRepository).findByCartAndOptionalNavAndPa(
            "CART-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        );
    }

    @Test
    void searchesHistoryByTokenAndIncludesOnlinePositionAndTokenIds() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByTokenAndOptionalNavAndPa("TOKEN-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.findByTokenAndOptionalNavAndPa(
            "TOKEN-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        )).thenReturn(new PageImpl<>(List.<Object[]>of(new Object[] { "NAV-1", "PA-1" }), pageable, 1));

        var result = service.searchByToken("PA-1", "NAV-1", " TOKEN-1 ", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-1");
            assertThat(item.getPaEmittente()).isEqualTo("PA-1");
        });
        verify(onlineRepository).findByTokenAndOptionalNavAndPa("TOKEN-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).findByTokenAndOptionalNavAndPa(
            "TOKEN-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            List.of(12, 56),
            List.of(34, 78),
            pageable
        );
    }

    @Test
    void searchesHistoryByTokenWhenOnlineSearchHasNoResults() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findByTokenAndOptionalNavAndPa("TOKEN-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.findByTokenAndOptionalNavAndPa(
            "TOKEN-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        ))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.searchByToken(null, null, "TOKEN-1", dateFrom, dateTo, pageable);

        assertThat(result).isEmpty();
        verify(historyRepository).findByTokenAndOptionalNavAndPa(
            "TOKEN-1",
            null,
            null,
            dateFrom,
            dateTo,
            List.of(),
            List.of(),
            pageable
        );
    }

    @Test
    void searchesHistoryByExtraAndIncludesOnlinePositionAndTokenIds() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findGroupedByExtraValueAndOptionalNavAndPa("EXTRA-1", "NAV-1", "PA-1"))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.findGroupedByExtraValueAndOptionalNavAndPa(
            "EXTRA-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            "12,56",
            "34,78"
        )).thenReturn(List.<Object[]>of(new Object[] { "NAV-1", "PA-1" }));

        var result = service.searchExtra("PA-1", "NAV-1", "EXTRA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-1");
            assertThat(item.getPaEmittente()).isEqualTo("PA-1");
        });
        verify(onlineRepository).findGroupedByExtraValueAndOptionalNavAndPa("EXTRA-1", "NAV-1", "PA-1");
        verify(historyRepository).findGroupedByExtraValueAndOptionalNavAndPa(
            "EXTRA-1",
            "NAV-1",
            "PA-1",
            dateFrom,
            dateTo,
            "12,56",
            "34,78"
        );
    }

    @Test
    void searchesHistoryByExtraWhenOnlineSearchHasNoResults() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.findGroupedByExtraValueAndOptionalNavAndPa("EXTRA-1", null, null))
            .thenReturn(List.of());
        when(historyRepository.findGroupedByExtraValueAndOptionalNavAndPa(
            "EXTRA-1",
            null,
            null,
            dateFrom,
            dateTo,
            "",
            ""
        )).thenReturn(List.of());

        var result = service.searchExtra(null, null, "EXTRA-1", dateFrom, dateTo, pageable);

        assertThat(result).isEmpty();
        verify(historyRepository).findGroupedByExtraValueAndOptionalNavAndPa(
            "EXTRA-1",
            null,
            null,
            dateFrom,
            dateTo,
            "",
            ""
        );
    }
}
