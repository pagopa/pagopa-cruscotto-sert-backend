package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.repository.history.HistoryPositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.online.OnlinePositionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class SertHistoryDataServiceImplTest {

    private final HistoryPositionRepository historyRepository = mock(HistoryPositionRepository.class);
    private final OnlinePositionRepository onlineRepository = mock(OnlinePositionRepository.class);
    private final SertHistoryDataServiceImpl service = new SertHistoryDataServiceImpl(historyRepository, onlineRepository);

    @Test
    void getsHistoryPositionWithPositionAndTokenIdsInFirstTwoColumns() {
        var pageable = PageRequest.of(0, 10);
        Object[] row = new Object[24];
        row[0] = 12;
        row[1] = 34;
        row[2] = "NAV-1";
        row[3] = "PA-1";
        row[4] = java.time.Instant.parse("2026-01-01T00:00:00Z");
        row[5] = "IUV-1";
        row[7] = "61626364";
        row[8] = java.sql.Date.valueOf("2026-01-01");
        row[9] = java.time.Instant.parse("2026-01-02T00:00:00Z");
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        when(historyRepository.findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), pageable, 1));
        when(historyRepository.countOkTokensByNavAndPa("NAV-1", "PA-1")).thenReturn(1L);

        var result = service.getPosition("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPositionInfo().getNav()).isEqualTo("NAV-1");
        assertThat(result.getContent().get(0).getPositionInfo().getPaEmittente()).isEqualTo("PA-1");
        verify(historyRepository).findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged());
        verify(onlineRepository).findPositionDetailRows("NAV-1", "PA-1", "12", "34", Pageable.unpaged());
    }

    @Test
    void mergesHistoryAndPartialOnlineTokenRowsAndSortsTokensByBornDate() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("tokenDateEvent")));
        Object[] historyFirst = positionRow(1, 10, "NAV-1", "6161", Instant.parse("2026-01-01T00:00:00Z"));
        Object[] historySecond = positionRow(1, 11, "NAV-1", "6262", Instant.parse("2026-01-02T00:00:00Z"));
        Object[] partialOnline = new Object[24];
        partialOnline[0] = 1;
        partialOnline[1] = 10;
        partialOnline[7] = "6161";
        partialOnline[8] = Instant.parse("2026-01-03T00:00:00Z");
        Object[] onlineOnly = positionRow(2, 12, "NAV-1", "6363", Instant.parse("2026-01-04T00:00:00Z"));

        when(historyRepository.findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.of(historyFirst, historySecond)));
        when(onlineRepository.findPositionDetailRows("NAV-1", "PA-1", "1,1", "10,11", Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.of(partialOnline, onlineOnly)));

        var result = service.getPosition("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(position -> {
            assertThat(position.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(position.getPositionInfo().getPaEmittente()).isEqualTo("PA-1");
            assertThat(position.getAllTokens()).containsExactly("cc", "aa", "bb");
        });
    }

    @Test
    void returnsPositionFromOnlineWhenHistoryRowsAreEmpty() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(0, 10);
        Object[] onlineRow = positionRow(2, 12, "NAV-1", "6363", Instant.parse("2026-01-04T00:00:00Z"));

        when(historyRepository.findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.<Object[]>of()));
        when(onlineRepository.findPositionDetailRows("NAV-1", "PA-1", "", "", Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.<Object[]>of(onlineRow)));

        var result = service.getPosition("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(position -> {
            assertThat(position.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(position.getAllTokens()).containsExactly("cc");
        });
    }

    private Object[] positionRow(int positionId, int tokenId, String nav, String tokenHex, Instant tokenBorn) {
        Object[] row = new Object[24];
        row[0] = positionId;
        row[1] = tokenId;
        row[2] = nav;
        row[3] = "PA-1";
        row[4] = Instant.parse("2026-01-01T00:00:00Z");
        row[5] = "IUV-1";
        row[7] = tokenHex;
        row[8] = tokenBorn;
        return row;
    }

    @Test
    void searchesHistoryByIuvAndIncludesOnlinePositionAndTokenIds() {
        var pageable = PageRequest.of(0, 10);
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 6, 30);
        when(onlineRepository.searchByIuv("IUV-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.searchByIuv(
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
        verify(onlineRepository).searchByIuv("IUV-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).searchByIuv(
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
        when(onlineRepository.searchByIuv("IUV-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.searchByIuv(
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
        verify(historyRepository).searchByIuv(
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
        when(onlineRepository.searchByCart("CART-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.searchByCart(
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
        verify(onlineRepository).searchByCart("CART-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).searchByCart(
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
        when(onlineRepository.searchByCart("CART-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.searchByCart(
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
        verify(historyRepository).searchByCart(
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
        when(onlineRepository.searchByToken("TOKEN-1", "NAV-1", "PA-1", Pageable.unpaged()))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.searchByToken(
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
        verify(onlineRepository).searchByToken("TOKEN-1", "NAV-1", "PA-1", Pageable.unpaged());
        verify(historyRepository).searchByToken(
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
        when(onlineRepository.searchByToken("TOKEN-1", null, null, Pageable.unpaged()))
            .thenReturn(List.of());
        when(historyRepository.searchByToken(
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
        verify(historyRepository).searchByToken(
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
        when(onlineRepository.searchExtra("EXTRA-1", "NAV-1", "PA-1"))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }, new Object[] { 56L, 78L }));
        when(historyRepository.searchExtra(
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
        verify(onlineRepository).searchExtra("EXTRA-1", "NAV-1", "PA-1");
        verify(historyRepository).searchExtra(
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
        when(onlineRepository.searchExtra("EXTRA-1", null, null))
            .thenReturn(List.of());
        when(historyRepository.searchExtra(
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
        verify(historyRepository).searchExtra(
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
