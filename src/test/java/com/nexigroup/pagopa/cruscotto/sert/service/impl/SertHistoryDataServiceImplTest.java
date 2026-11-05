package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.repository.history.HistoryPositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.online.OnlinePositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
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
    void getsTokenInfoWithDateRangeAndPositionIds() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        Object[] row = new Object[24];
        row[0] = 12;
        row[1] = 34;
        row[2] = "NAV-1";
        row[3] = "PA-1";
        row[4] = Instant.parse("2026-01-01T00:00:00Z");
        row[5] = "IUV-1";
        row[6] = "CRID-1";
        row[7] = "61626364";
        row[8] = java.sql.Date.valueOf("2026-01-02");
        row[9] = Instant.parse("2026-01-03T00:00:00Z");
        row[10] = "OK";
        row[11] = new java.math.BigDecimal("12.50");
        row[12] = new java.math.BigDecimal("1.50");
        row[13] = "PSP";
        row[14] = "PT-PA";
        row[15] = "PT-PSP";
        row[16] = "STATION";
        row[17] = "CHANNEL";
        row[18] = "TOUCHPOINT";
        row[19] = "CARD";
        row[20] = "CART-1";
        row[21] = "PSP description";
        row[22] = "PT-PA description";
        row[23] = "PT-PSP description";
        when(onlineRepository.findTokenDetailRow("abcd")).thenReturn(List.of());
        when(historyRepository.findTokenDetailRow("abcd", dateFrom, dateTo, List.of()))
            .thenReturn(List.<Object[]>of(row));
        when(historyRepository.searchByNavIds("NAV-1", "PA-1", dateFrom, dateTo))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }));
        when(historyRepository.countOkTokensByNavAndPa("NAV-1", "PA-1")).thenReturn(1L);

        var result = service.getTokenInfo(" abcd ", dateFrom, dateTo);

        assertThat(result.getPositionInfo().getNav()).isEqualTo("NAV-1");
        assertThat(result.getPositionInfo().getIuv()).isEqualTo("IUV-1");
        assertThat(result.getPayed().getToken()).isEqualTo("abcd");
        assertThat(result.getAmount().getAmount()).isEqualTo(12.5);
        assertThat(result.getActors().getPsp()).isEqualTo("PSP");
        assertThat(result.getPaymentInfo().getIdCarrello()).isEqualTo("CART-1");
        verify(onlineRepository).findTokenDetailRow("abcd");
        verify(historyRepository).findTokenDetailRow("abcd", dateFrom, dateTo, List.of());
    }

    @Test
    void sortsOnlineTokenRowsAndReturnsTheRequestedToken() {
        Object[] otherToken = positionRow(1, 35, "NAV-1", "65666768", Instant.parse("2026-01-03T00:00:00Z"));
        otherToken[5] = "OTHER-IUV";
        Object[] requestedToken = positionRow(1, 34, "NAV-1", "61626364", Instant.parse("2026-01-02T00:00:00Z"));
        requestedToken[5] = "REQUESTED-IUV";
        requestedToken[9] = Instant.parse("2026-01-04T00:00:00Z");
        requestedToken[10] = "OK";
        when(onlineRepository.findTokenDetailRow("abcd"))
            .thenReturn(List.of(otherToken, requestedToken));
        when(historyRepository.findTokenDetailRow(
            "abcd",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            List.of(1)
        )).thenReturn(List.<Object[]>of());
        when(historyRepository.searchByNavIds(
            "NAV-1",
            "PA-1",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31)
        )).thenReturn(List.<Object[]>of(new Object[] { 1, 34 }));

        var result = service.getTokenInfo(
            "abcd",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31)
        );

        assertThat(result.getPositionInfo().getIuv()).isEqualTo("REQUESTED-IUV");
        assertThat(result.getPayed().getToken()).isEqualTo("abcd");
    }

    @Test
    void mapsPositionAndTokenInfoFromDifferentSourceRows() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        Object[] offlinePositionRow = new Object[24];
        offlinePositionRow[0] = 12;
        offlinePositionRow[1] = 34;
        offlinePositionRow[2] = "NAV-OFFLINE";
        offlinePositionRow[3] = "PA-OFFLINE";
        offlinePositionRow[4] = Instant.parse("2026-01-01T00:00:00Z");
        Object[] onlineTokenRow = new Object[24];
        onlineTokenRow[0] = 12L;
        onlineTokenRow[1] = 34L;
        onlineTokenRow[5] = "IUV-ONLINE";
        onlineTokenRow[6] = "CRID-ONLINE";
        onlineTokenRow[7] = "61626364";
        onlineTokenRow[8] = java.sql.Date.valueOf("2026-01-02");
        onlineTokenRow[9] = Instant.parse("2026-01-03T00:00:00Z");
        onlineTokenRow[10] = "OK";
        onlineTokenRow[11] = new java.math.BigDecimal("12.50");
        onlineTokenRow[12] = new java.math.BigDecimal("1.50");
        onlineTokenRow[13] = "PSP-ONLINE";
        when(onlineRepository.findTokenDetailRow("abcd"))
            .thenReturn(List.<Object[]>of(onlineTokenRow));
        when(historyRepository.findTokenDetailRow("abcd", dateFrom, dateTo, List.of(12)))
            .thenReturn(List.<Object[]>of(offlinePositionRow));
        when(historyRepository.searchByNavIds("NAV-OFFLINE", "PA-OFFLINE", dateFrom, dateTo))
            .thenReturn(List.<Object[]>of(new Object[] { 12, 34 }));
        when(historyRepository.countOkTokensByNavAndPa("NAV-OFFLINE", "PA-OFFLINE")).thenReturn(1L);

        var result = service.getTokenInfo("abcd", dateFrom, dateTo);

        assertThat(result.getPositionInfo().getNav()).isEqualTo("NAV-OFFLINE");
        assertThat(result.getPositionInfo().getPaEmittente()).isEqualTo("PA-OFFLINE");
        assertThat(result.getPositionInfo().getLastEvent()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(result.getPositionInfo().getIuv()).isEqualTo("IUV-ONLINE");
        assertThat(result.getPayed().getToken()).isEqualTo("abcd");
        assertThat(result.getAmount().getAmount()).isEqualTo(12.5);
        assertThat(result.getActors().getPsp()).isEqualTo("PSP-ONLINE");
    }

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
        var result = service.getPosition("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPositionInfo().getNav()).isEqualTo("NAV-1");
        assertThat(result.getContent().get(0).getPositionInfo().getPaEmittente()).isEqualTo("PA-1");
        verify(historyRepository).findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged());
        verify(onlineRepository).findPositionDetailRows(
            "NAV-1",
            "PA-1",
            List.of(12),
            List.of(34),
            Pageable.unpaged()
        );
    }

    @Test
    void mergesHistoryAndPartialOnlineTokenRowsAndSortsTokensByBornDate() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("tokenDateEvent")));
        Object[] historyFirst = positionRow(1, 10, "NAV-1", "6161", Instant.parse("2026-01-01T00:00:00Z"));
        Object[] historySecond = positionRow(1, 11, "NAV-1", "6262", Instant.parse("2026-01-02T00:00:00Z"));
        historyFirst[10] = "OK";
        historySecond[10] = "KO";
        Object[] partialOnline = new Object[24];
        partialOnline[0] = 1;
        partialOnline[1] = 10;
        partialOnline[7] = "6161";
        partialOnline[8] = Instant.parse("2026-01-03T00:00:00Z");
        partialOnline[10] = "KO";
        Object[] onlineOnly = positionRow(2, 12, "NAV-1", "6363", Instant.parse("2026-01-04T00:00:00Z"));
        onlineOnly[9] = Instant.parse("2026-01-05T00:00:00Z");
        onlineOnly[10] = "ok";

        when(historyRepository.findPositionDetailRows("NAV-1", "PA-1", dateFrom, dateTo, Pageable.unpaged()))
            .thenReturn(new PageImpl<>(List.of(historyFirst, historySecond)));
        when(onlineRepository.findPositionDetailRows(
            "NAV-1",
            "PA-1",
            List.of(1, 1),
            List.of(10, 11),
            Pageable.unpaged()
        ))
            .thenReturn(new PageImpl<>(List.of(partialOnline, onlineOnly)));

        var result = service.getPosition("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(position -> {
            assertThat(position.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(position.getPositionInfo().getPaEmittente()).isEqualTo("PA-1");
            assertThat(position.getAllTokens()).containsExactly("cc", "aa", "bb");
            assertThat(position.getPayed().getMultiOutcome()).isTrue();
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
        when(onlineRepository.findPositionDetailRows("NAV-1", "PA-1", List.of(), List.of(), Pageable.unpaged()))
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

    @Test
    void mergesTransferRowsSortsAndPaginatesTransfers() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "amount"));
        Object[] positionTokenRow = transferRow(20, null, null);
        positionTokenRow[0] = Position.builder().id(10).nav("NAV-1").build();
        positionTokenRow[3] = "NAV-1";
        positionTokenRow[4] = "PA-1";
        positionTokenRow[5] = Instant.parse("2026-01-01T00:00:00Z");
        positionTokenRow[6] = "IUV-1";
        positionTokenRow[7] = "CRID-1";
        positionTokenRow[8] = "61626364";
        positionTokenRow[9] = 3L;
        Object[] onlineTransfer = transferRow(20, 1, new java.math.BigDecimal("8.00"));
        Object[] otherTokenTransfer = transferRow(21, 4, new java.math.BigDecimal("50.00"));
        otherTokenTransfer[8] = "6f74686572";
        Object[] historyTransfer = transferRow(20, 3, new java.math.BigDecimal("13.00"));
        when(historyRepository.findByTokenNoPage("abcd", "NAV-1", "PA-1", dateFrom, dateTo))
            .thenReturn(List.<Object[]>of(new Object[] { 10, 20 }));
        when(onlineRepository.findTransferDetailRows("abcd", List.of(20)))
            .thenReturn(List.<Object[]>of(onlineTransfer));
        when(historyRepository.findTransferDetailRows("NAV-1", "PA-1", "abcd"))
            .thenReturn(List.<Object[]>of(otherTokenTransfer, positionTokenRow, historyTransfer));

        var result = service.getTransfers("NAV-1", "PA-1", " abcd ", dateFrom, dateTo, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(item.getPositionInfo().getPaEmittente()).isEqualTo("PA-1");
            assertThat(item.getPositionInfo().getIuv()).isEqualTo("IUV-1");
            assertThat(item.getPositionInfo().getCreditorReferenceId()).isEqualTo("CRID-1");
            assertThat(item.getToken()).isEqualTo("abcd");
            assertThat(item.getTransfersCount()).isEqualTo(3.0);
            assertThat(item.getTransfers()).singleElement().satisfies(transfer -> {
                assertThat(transfer.getIdTransfer()).isEqualTo(1);
                assertThat(transfer.getAmount()).isEqualTo(8.0);
            });
        });
    }

    @Test
    void mapsPositionDetailsFromSeparateHistoryRowWhenTokenIsOnline() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(0, 10);
        Object[] onlineTokenRow = transferRow(20, null, null);
        onlineTokenRow[6] = "IUV-ONLINE";
        onlineTokenRow[7] = "CRID-ONLINE";
        onlineTokenRow[8] = "61626364";
        Object[] historyPositionRow = transferRow(null, null, null);
        historyPositionRow[0] = Position.builder()
            .id(10)
            .nav("NAV-1")
            .paEmittente("PA-1")
            .build();
        historyPositionRow[1] = 10;
        historyPositionRow[3] = "NAV-1";
        historyPositionRow[4] = "PA description";
        historyPositionRow[5] = Instant.parse("2026-01-01T00:00:00Z");
        Object[] onlineTransfer = transferRow(20, 1, new java.math.BigDecimal("8.00"));
        when(historyRepository.findByTokenNoPage("abcd", "NAV-1", "PA-1", dateFrom, dateTo))
            .thenReturn(List.<Object[]>of(new Object[] { 10, 20 }));
        when(onlineRepository.findTransferDetailRows("abcd", List.of(20)))
            .thenReturn(List.<Object[]>of(onlineTokenRow, onlineTransfer));
        when(historyRepository.findTransferDetailRows("NAV-1", "PA-1", "abcd"))
            .thenReturn(List.<Object[]>of(historyPositionRow));

        var result = service.getTransfers("NAV-1", "PA-1", "abcd", dateFrom, dateTo, pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(item.getPositionInfo().getPaEmittente()).isEqualTo("PA description");
            assertThat(item.getPositionInfo().getLastEvent()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(item.getPositionInfo().getIuv()).isEqualTo("IUV-ONLINE");
            assertThat(item.getPositionInfo().getCreditorReferenceId()).isEqualTo("CRID-ONLINE");
            assertThat(item.getToken()).isEqualTo("abcd");
            assertThat(item.getTransfers()).singleElement().satisfies(transfer ->
                assertThat(transfer.getIdTransfer()).isEqualTo(1)
            );
        });
    }

    @Test
    void mergesWorkflowRowsUsingPositionAndTokenIdsFromHistory() {
        var dateFrom = LocalDate.of(2026, 1, 1);
        var dateTo = LocalDate.of(2026, 1, 31);
        var pageable = PageRequest.of(0, 10);
        Object[] historyWorkflow = workflowRow("HISTORY-EVENT", "61626364");
        Object[] onlineWorkflow = workflowRow("ONLINE-EVENT", "61626364");
        when(historyRepository.findByTokenNoPage(null, "NAV-1", "PA-1", dateFrom, dateTo))
            .thenReturn(List.<Object[]>of(new Object[] { 10, 20 }));
        when(historyRepository.findPositionWorkflows(
            "NAV-1",
            "PA-1",
            List.of(10),
            List.of(20)
        )).thenReturn(List.<Object[]>of(historyWorkflow));
        when(onlineRepository.findPositionWorkflows(
            "NAV-1",
            "PA-1",
            List.of(10),
            List.of(20)
        )).thenReturn(List.<Object[]>of(onlineWorkflow));

        var result = service.getWorkflows("NAV-1", "PA-1", dateFrom, dateTo, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).singleElement().satisfies(item ->
            assertThat(item.getEventsPosition())
                .extracting(event -> event.getEventId())
                .containsExactly("HISTORY-EVENT", "ONLINE-EVENT")
        );
        verify(historyRepository).findByTokenNoPage(null, "NAV-1", "PA-1", dateFrom, dateTo);
    }

    private Object[] workflowRow(String eventId, String token) {
        Object[] row = new Object[9];
        row[0] = Instant.parse("2026-01-01T00:00:00Z");
        row[1] = "EVENT";
        row[2] = "REQ/RESP";
        row[3] = "OK";
        row[4] = eventId;
        row[6] = token;
        row[7] = "RESP";
        row[8] = eventId;
        return row;
    }

    private Object[] transferRow(Integer positionTokenId, Integer transferId, java.math.BigDecimal amount) {
        Object[] row = new Object[16];
        row[1] = 10;
        row[2] = positionTokenId;
        if (transferId != null) {
            row[10] = transferId;
            row[11] = Boolean.FALSE;
            row[12] = "IBAN";
            row[13] = amount;
            row[14] = "PA-FISCAL";
            row[15] = LocalDate.of(2026, 1, 2);
        }
        return row;
    }
}
