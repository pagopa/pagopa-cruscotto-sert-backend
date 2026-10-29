package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.repository.online.OnlinePositionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class SertOnlineServiceImplTest {

    private static final LocalDate DATE_FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.of(2026, 1, 31);

    private final OnlinePositionRepository onlinePositionRepository = mock(OnlinePositionRepository.class);
    private final SertOnlineServiceImpl service = new SertOnlineServiceImpl(onlinePositionRepository);

    @Test
    void skipsEmptyTokenSearchAndMapsNavSearchRows() {
        var pageable = PageRequest.of(0, 10);
        assertThat(service.searchByToken("pa", "nav", "   ", LocalDate.now(), LocalDate.now(), pageable)).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(onlinePositionRepository);

        when(onlinePositionRepository.findByNavOrPa("nav-1", "pa-1", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(new Object[] { "nav-1", "pa-1" }), pageable, 1));

        var result = service.searchByNav("nav-1", "pa-1", LocalDate.now(), LocalDate.now(), pageable);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("nav-1");
            assertThat(item.getPaEmittente()).isEqualTo("pa-1");
        });
    }

    @Test
    void searchesExtraInfoSortsAndPagesMappedRows() {
        var pageable = PageRequest.of(0, 1, Sort.by("nav"));
        when(onlinePositionRepository.findGroupedByExtraValueAndOptionalNavAndPa("reason", null, null)).thenReturn(List.<Object[]>of(
            new Object[] { "NAV-B", "PA-B" },
            new Object[] { "NAV-A", "PA-A" }
        ));

        var result = service.searchExtra(null, null, "reason", LocalDate.now(), LocalDate.now(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNav()).isEqualTo("NAV-A");
            assertThat(item.getPaEmittente()).isEqualTo("PA-A");
            assertThat(item.getPaEmittenteDsc()).isNull();
            assertThat(item.getMatch()).isNull();
        });
        verify(onlinePositionRepository).findGroupedByExtraValueAndOptionalNavAndPa("reason", null, null);
    }

    @Test
    void mapsPositionDetailsAfterRemovingUnusedOutcomeColumn() {
        var pageable = PageRequest.of(0, 10);
        Object[] row = new Object[21];
        row[0] = "NAV-1";
        row[1] = "PA description";
        row[2] = Instant.parse("2026-01-01T00:00:00Z");
        row[3] = "IUV-1";
        row[4] = "CRID-1";
        row[5] = "61626364";
        row[6] = java.sql.Date.valueOf("2026-01-02");
        row[7] = Instant.parse("2026-01-03T00:00:00Z");
        row[8] = new java.math.BigDecimal("12.50");
        row[9] = new java.math.BigDecimal("1.50");
        row[10] = "PSP";
        row[11] = "PT-PA";
        row[12] = "PT-PSP";
        row[13] = "STATION";
        row[14] = "CHANNEL";
        row[15] = "TOUCHPOINT";
        row[16] = "CARD";
        row[17] = "CART-1";
        row[18] = "PSP description";
        row[19] = "PT-PA description";
        row[20] = "PT-PSP description";
        when(onlinePositionRepository.findPositionDetailRows("NAV-1", "PA-1", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), pageable, 1));
        when(onlinePositionRepository.countOkTokensByNavAndPa("NAV-1", "PA-1")).thenReturn(1L);

        var result = service.getPosition("NAV-1", "PA-1", pageable);

        assertThat(result.getContent()).singleElement().satisfies(position -> {
            assertThat(position.getPositionInfo().getNav()).isEqualTo("NAV-1");
            assertThat(position.getPositionInfo().getPaEmittente()).isEqualTo("PA description");
            assertThat(position.getPositionInfo().getIuv()).isEqualTo("IUV-1");
            assertThat(position.getAmount().getAmount()).isEqualTo(12.5);
            assertThat(position.getAmount().getFee()).isEqualTo(1.5);
            assertThat(position.getActors().getPsp()).isEqualTo("PSP");
            assertThat(position.getActors().getPtPa()).isEqualTo("PT-PA");
            assertThat(position.getActors().getPtPsp()).isEqualTo("PT-PSP");
            assertThat(position.getActors().getStation()).isEqualTo("STATION");
            assertThat(position.getActors().getChannel()).isEqualTo("CHANNEL");
            assertThat(position.getActors().getPspDescription()).isEqualTo("PSP description");
            assertThat(position.getActors().getPtPaDescription()).isEqualTo("PT-PA description");
            assertThat(position.getActors().getPtPspDescription()).isEqualTo("PT-PSP description");
            assertThat(position.getPaymentInfo().getTouchpoint()).isEqualTo("TOUCHPOINT");
            assertThat(position.getPaymentInfo().getPaymentMethod()).isEqualTo("CARD");
            assertThat(position.getPaymentInfo().getIsCart()).isTrue();
        });
    }

    @Test
    void returnsEmptyForNullExtraSearchAndMapsTokenInfoPaymentFields() {
        var pageable = PageRequest.of(0, 10);
        assertThat(service.searchExtra(null, null, null,  LocalDate.now(), LocalDate.now(), pageable)).isEmpty();

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
        when(onlinePositionRepository.findTokenDetailRow("61626364")).thenReturn(List.<Object[]>of(row));
        when(onlinePositionRepository.countOkTokensByNavAndPa("351234567890123456", "PA-1")).thenReturn(2L);

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

    @Test
    void mapsTransferRowsAndReturnsNullWhenRequiredSearchKeysAreMissing() {
        var pageable = PageRequest.of(0, 10);
        Object[] row = new Object[12];
        row[0] = "NAV-1";
        row[1] = "PA-1";
        row[2] = Instant.parse("2026-02-01T00:00:00Z");
        row[3] = "IUV-1";
        row[4] = "CRID-1";
        row[5] = "61626364";
        row[6] = 1;
        row[7] = "7";
        row[8] = true;
        row[9] = "IBAN-1";
        row[10] = new java.math.BigDecimal("25.00");
        row[11] = "FISCAL-1";
        when(onlinePositionRepository.findTransferDetailRows("NAV-1", "PA-1", "61626364", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), pageable, 1));

        var result = service.getTransfers("NAV-1", "PA-1", " 61626364 ", DATE_FROM, DATE_TO, pageable);

        assertThat(result.getContent()).singleElement().satisfies(transferPage -> {
            assertThat(transferPage.getToken()).isEqualTo("abcd");
            assertThat(transferPage.getTransfersCount()).isEqualTo(1.0);
            assertThat(transferPage.getTransfers()).singleElement().satisfies(transfer -> {
                assertThat(transfer.getIdTransfer()).isEqualTo(7);
                assertThat(transfer.getTypeTransfer()).isEqualTo("bollo");
                assertThat(transfer.getIban()).isEqualTo("IBAN-1");
                assertThat(transfer.getPaFiscalCode()).isEqualTo("FISCAL-1");
                assertThat(transfer.getAmount()).isEqualTo(25.0);
            });
        });
        assertThat(service.getTransfers(null, "PA-1", "token", DATE_FROM, DATE_TO, pageable)).isNull();
    }

    @Test
    void mapsWorkflowEventsAndFiltersExtraInfoRowsWithoutName() {
        var pageable = PageRequest.of(0, 10);
        Object[] event = new Object[9];
        event[0] = Instant.parse("2026-02-01T00:00:00Z");
        event[1] = "payment";
        event[2] = "REQ/RESP";
        event[3] = "OK";
        event[4] = "event-1";
        event[5] = "fault";
        event[6] = "61626364";
        event[7] = "REQ";
        event[8] = "id-1";
        when(onlinePositionRepository.findPositionWorkflows("NAV-1", "PA-1", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(event), pageable, 1));
        var workflows = service.getWorkflows("NAV-1", "PA-1", pageable);
        assertThat(workflows.getContent()).singleElement().satisfies(response -> {
            assertThat(response.getEventsPosition()).singleElement().satisfies(item -> {
                assertThat(item.getSottotipoevento()).isEqualTo("REQ");
                assertThat(item.getFaultcode()).isNull();
            });
        });

        Object[] visible = new Object[6];
        visible[0] = "NAV-1";
        visible[1] = "PA-1";
        visible[2] = "61626364";
        visible[3] = "info-name";
        visible[4] = "value";
        visible[5] = "event";
        Object[] nameless = new Object[] { "NAV-2", "PA-2", "61626364", null, "ignored", "event" };
        when(onlinePositionRepository.findExtraInfoByToken("token", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(visible, nameless), pageable, 2));

        var extraInfo = service.getExtraInfo("token", pageable);
        assertThat(extraInfo.getContent()).singleElement().satisfies(response -> {
            assertThat(response.getCount()).isEqualTo(2L);
            assertThat(response.getResults()).singleElement().satisfies(item -> {
                assertThat(item.getName()).isEqualTo("info-name");
                assertThat(item.getValue()).isEqualTo("value");
                assertThat(item.getToken()).isEqualTo("abcd");
            });
        });
    }
}
