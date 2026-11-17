package com.nexigroup.pagopa.cruscotto.sert.service.fdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexigroup.pagopa.cruscotto.sert.service.FdrService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrSearchedBy;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionSummaryDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.OccurrenceResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReingestionBatchDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.FdrServiceImpl;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

class FdrServiceTest {

    private final FdrService service = new FdrServiceImpl();

    @Test
    void returnsFlowRevisionPageWithContractMetadata() {
        Page<FlowRevisionSummaryDTO> response = service.searchFlows("FLOW-42", null, null, "ALL", null, PageRequest.of(0, 1));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getContent().get(0).flowId()).isEqualTo("FLOW-42");
        assertThat(response.getContent().get(0).revisionId()).isNotNull();
    }

    @Test
    void filtersOccurrenceMockByArrivalDay() {
        Page<OccurrenceResultDTO> response = service.searchOccurrences(
            FdrSearchedBy.IUV,
            "IUV-42",
            null,
            null,
            LocalDate.of(2026, 11, 15),
            LocalDate.of(2026, 11, 15),
            "ONLINE",
            PageRequest.of(0, 50)
        );

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    void marksHistoricalOccurrenceAsNotLatestRevision() {
        Page<OccurrenceResultDTO> response = service.searchOccurrences(
            FdrSearchedBy.IUV,
            "IUV-42",
            null,
            null,
            null,
            null,
            "HISTORICAL",
            PageRequest.of(0, 50)
        );
        OccurrenceResultDTO occurrence = response.getContent().get(0);
        assertThat(occurrence.revisionNumber()).isEqualTo(1);
        assertThat(occurrence.totalFlowRevisions()).isEqualTo(2);
        assertThat(occurrence.flow().isLatestRevision()).isFalse();
    }

    @Test
    void searchesEveryMatchingRevisionAndExportsCsv() throws Exception {
        Page<OccurrenceResultDTO> response = service.searchOccurrences(
            FdrSearchedBy.IUV_AND_FLOW_ID,
            "IUV-42",
            "FLOW-42",
            null,
            null,
            null,
            "ALL",
            PageRequest.of(0, 50)
        );
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent()).extracting(OccurrenceResultDTO::revisionNumber).containsExactly(2, 1);
        assertThat(response.getContent()).allSatisfy(occurrence -> assertThat(occurrence.flow().flowId()).isEqualTo("FLOW-42"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportRevisionCsv(java.util.UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000001"), output);
        String csv = output.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("\"recordId\",\"revisionId\"").contains("\"12.50\"").contains("\"dateInserted\"");
    }

    @Test
    void createsIdempotentInMemoryReingestionMocks() {
        List<LocalDate> arrivalDates = List.of(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 15));
        ReingestionBatchDTO first = service.createReingestion("mock-key-001", arrivalDates, "Mock request");
        ReingestionBatchDTO repeated = service.createReingestion("mock-key-001", arrivalDates, "Mock request");

        assertThat(first.requestId()).isEqualTo(repeated.requestId());
        List<LocalDate> sortedDates = first.items().stream().map(item -> item.arrivalDate()).toList();
        assertThat(sortedDates).isEqualTo(List.of(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 16)));
        assertThatThrownBy(() -> service.createReingestion("mock-key-001", arrivalDates, "Different request"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(409));
    }
}
