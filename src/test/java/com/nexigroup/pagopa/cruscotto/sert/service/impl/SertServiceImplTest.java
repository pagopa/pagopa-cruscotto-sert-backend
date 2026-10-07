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
        when(positionRepository.findTransferDetailRows("NAV-1", "PA-1", "61626364", pageable))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), pageable, 1));

        var result = service.getTransfers("NAV-1", "PA-1", " 61626364 ", pageable);

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
        assertThat(service.getTransfers(null, "PA-1", "token", pageable)).isNull();
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
        when(positionRepository.findPositionWorkflows("NAV-1", "PA-1", pageable))
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
        when(positionRepository.findExtraInfoByToken("token", pageable))
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
