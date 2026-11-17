package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.service.FdrService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrAsyncStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrDataScope;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrEventLevel;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrEventSourceCode;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrIngestionDayStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrReingestionDayStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrSearchedBy;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionDetailDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionSummaryDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionDayStatusDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionEventDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.OccurrenceResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReingestionBatchDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReingestionDayItemDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReportedPaymentRowDTO;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FdrServiceImpl implements FdrService {

    private static final UUID REVISION_1 = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000001");
    private static final UUID REVISION_2 = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000002");
    private static final UUID MOCK_BATCH_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000003");
    private static final UUID MOCK_OCCURRENCE_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000004");
    private static final UUID MOCK_PAYMENT_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000005");
    private static final UUID MOCK_EXECUTION_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000006");
    private static final UUID MOCK_HISTORICAL_OCCURRENCE_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000007");
    private static final UUID MOCK_HISTORICAL_PAYMENT_ID = UUID.fromString("9b70cfa7-299b-42da-b5c8-000000000008");
    private static final LocalDate ARRIVAL_DAY = LocalDate.of(2026, 11, 15);
    private static final LocalDate LATEST_ARRIVAL_DAY = LocalDate.of(2026, 11, 16);
    private static final String MOCK_CREDITOR_ID = "80000000001";

    private final ConcurrentMap<String, StoredBatch> batchesByKey = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ReingestionBatchDTO> batchesById = new ConcurrentHashMap<>();

    @Override
    public Page<FlowRevisionSummaryDTO> searchFlows(
        String flowId,
        LocalDate arrivalFrom,
        LocalDate arrivalTo,
        String scope,
        String creditorInstitutionId,
        Pageable pageable
    ) {
        validateSearchValue(flowId, "flowId");
        validateArrivalRange(arrivalFrom, arrivalTo);
        validatePageable(pageable);
        validateScope(scope);

        List<FlowRevisionSummaryDTO> revisions = new ArrayList<>();
        if ("ONLINE".equals(scope) || "ALL".equals(scope)) {
            revisions.add(flowSummary(REVISION_2, flowId, LATEST_ARRIVAL_DAY, 2, true, "ONLINE", creditorInstitutionId));
        }
        if ("HISTORICAL".equals(scope) || "ALL".equals(scope)) {
            revisions.add(flowSummary(REVISION_1, flowId, ARRIVAL_DAY, 1, false, "HISTORICAL", creditorInstitutionId));
        }
        return toPage(revisions, pageable);
    }

    @Override
    public FlowRevisionDetailDTO getRevision(UUID revisionId) {
        FlowRevisionSummaryDTO summary;
        if (REVISION_1.equals(revisionId)) {
            summary = flowSummary(REVISION_1, "MOCK-FLOW-001", ARRIVAL_DAY, 1, false, "HISTORICAL", MOCK_CREDITOR_ID);
        } else if (REVISION_2.equals(revisionId)) {
            summary = flowSummary(REVISION_2, "MOCK-FLOW-001", LATEST_ARRIVAL_DAY, 2, true, "ONLINE", MOCK_CREDITOR_ID);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Revisione FDR non trovata");
        }
        return FlowRevisionDetailDTO.from(
            summary,
            "Mock response FDR",
            List.of(ARRIVAL_DAY.atStartOfDay().toInstant(java.time.ZoneOffset.UTC), LATEST_ARRIVAL_DAY.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)),
            List.of("iuv", "iur", "creditorInstitutionId", "amount")
        );
    }

    @Override
    public Page<ReportedPaymentRowDTO> searchPayments(
        UUID revisionId,
        String iuv,
        String iur,
        BigDecimal amount,
        String creditorInstitutionId,
        String matchMode,
        Pageable pageable
    ) {
        validatePageable(pageable);
        validateMatchMode(matchMode);
        getRevision(revisionId);
        List<ReportedPaymentRowDTO> payments = List.of(
            paymentRow(
                revisionId,
                "MOCK-FLOW-001",
                iuv == null ? "IUV-MOCK-001" : iuv,
                iur,
                amount == null ? new BigDecimal("12.50") : amount,
                creditorInstitutionId
            )
        );
        return toPage(payments, pageable);
    }

    @Override
    public void exportRevisionCsv(UUID revisionId, OutputStream outputStream) throws IOException {
        getRevision(revisionId);
        ReportedPaymentRowDTO payment = paymentRow(
            revisionId,
            "MOCK-FLOW-001",
            "IUV-MOCK-001",
            "IUR-MOCK-001",
            new BigDecimal("12.50"),
            MOCK_CREDITOR_ID
        );
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writeCsvRow(
            writer,
            List.of(
                "recordId",
                "revisionId",
                "flowId",
                "sourceUniqueId",
                "iuv",
                "iur",
                "amount",
                "outcomeCode",
                "singlePaymentOutcomeTimestamp",
                "idsp",
                "flowDateTime",
                "pspId",
                "pspIntermediaryId",
                "insertedTimestamp",
                "creditorInstitutionId",
                "dateInserted"
            )
        );
        writeCsvRow(
            writer,
            java.util.Arrays.asList(
                payment.recordId().toString(),
                payment.revisionId().toString(),
                payment.flowId(),
                payment.sourceUniqueId(),
                payment.iuv(),
                payment.iur(),
                payment.amount().toPlainString(),
                payment.outcomeCode(),
                payment.singlePaymentOutcomeTimestamp().toString(),
                payment.idsp().toString(),
                payment.flowDateTime().toString(),
                payment.pspId(),
                payment.pspIntermediaryId(),
                payment.insertedTimestamp().toString(),
                payment.creditorInstitutionId(),
                payment.dateInserted().toString()
            )
        );
        writer.flush();
    }

    @Override
    public Page<OccurrenceResultDTO> searchOccurrences(
        FdrSearchedBy searchedBy,
        String searchedValue,
        String flowId,
        String creditorInstitutionId,
        LocalDate arrivalFrom,
        LocalDate arrivalTo,
        String scope,
        Pageable pageable
    ) {
        validateArrivalRange(arrivalFrom, arrivalTo);
        validatePageable(pageable);
        validateScope(scope);
        validateSearchValue(searchedValue, "searchedValue");

        if (flowId != null) {
            validateSearchValue(flowId, "flowId");
        }
        List<RevisionMock> matchingRevisions = switch (scope) {
            case "ONLINE" -> List.of(new RevisionMock(REVISION_2, LATEST_ARRIVAL_DAY, 2, true, "ONLINE"));
            case "HISTORICAL" -> List.of(new RevisionMock(REVISION_1, ARRIVAL_DAY, 1, false, "HISTORICAL"));
            case "ALL" -> List.of(
                new RevisionMock(REVISION_2, LATEST_ARRIVAL_DAY, 2, true, "ONLINE"),
                new RevisionMock(REVISION_1, ARRIVAL_DAY, 1, false, "HISTORICAL")
            );
            default -> throw new IllegalStateException("Scope was validated before use");
        };
        List<OccurrenceResultDTO> occurrences = new ArrayList<>();
        for (int index = 0; index < matchingRevisions.size(); index++) {
            RevisionMock revision = matchingRevisions.get(index);
            if (!matchesDate(revision.arrivalDate(), arrivalFrom, arrivalTo)) {
                continue;
            }
            String mockFlowId = flowId == null ? "MOCK-FLOW-001" : flowId;
            FlowRevisionSummaryDTO flow = flowSummary(
                revision.revisionId(),
                mockFlowId,
                revision.arrivalDate(),
                revision.ordinal(),
                revision.latest(),
                revision.dataArea(),
                creditorInstitutionId
            );
            ReportedPaymentRowDTO payment = paymentRow(
                revision.revisionId(),
                mockFlowId,
                searchedBy == FdrSearchedBy.IUR ? "IUV-MOCK-001" : searchedValue,
                searchedBy == FdrSearchedBy.IUR ? searchedValue : "IUR-MOCK-001",
                new BigDecimal("12.50"),
                creditorInstitutionId
            );
            UUID occurrenceId = revision.revisionId().equals(REVISION_1) ? MOCK_HISTORICAL_OCCURRENCE_ID : MOCK_OCCURRENCE_ID;
            occurrences.add(new OccurrenceResultDTO(occurrenceId, searchedBy, searchedValue, flow, payment, revision.ordinal(), 2));
        }
        return toPage(occurrences, pageable);
    }

    @Override
    public ReingestionBatchDTO createReingestion(String idempotencyKey, List<LocalDate> arrivalDates, String reason) {
        validateIdempotencyKey(idempotencyKey);
        validateReingestion(arrivalDates, reason);
        List<LocalDate> sortedDates = arrivalDates.stream().sorted().toList();
        String requestFingerprint = sortedDates + "|" + reason;
        StoredBatch stored = batchesByKey.compute(idempotencyKey, (key, existing) -> {
            if (existing != null && !existing.fingerprint().equals(requestFingerprint)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key già utilizzata per una richiesta diversa");
            }
            if (existing != null) {
                return existing;
            }
            return new StoredBatch(requestFingerprint, createBatch(UUID.randomUUID(), sortedDates));
        });
        ReingestionBatchDTO response = stored.batch();
        batchesById.putIfAbsent(response.requestId(), response);
        return response;
    }

    @Override
    public Page<ReingestionBatchDTO> listReingestions(String status, Pageable pageable) {
        validatePageable(pageable);
        List<ReingestionBatchDTO> batches = new ArrayList<>(batchesById.values());
        if (batches.stream().noneMatch(batch -> MOCK_BATCH_ID.equals(batch.requestId()))) {
            batches.add(mockBatch());
        }
        if (status != null) {
            batches.removeIf(batch -> !status.equals(batch.status().name()));
        }
        batches.sort(Comparator.comparing(ReingestionBatchDTO::createdAt).reversed());
        return toPage(batches, pageable);
    }

    @Override
    public ReingestionBatchDTO getReingestion(UUID requestId) {
        ReingestionBatchDTO batch = batchesById.get(requestId);
        if (batch != null) {
            return batch;
        }
        if (MOCK_BATCH_ID.equals(requestId)) {
            return mockBatch();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Richiesta di reingestion non trovata");
    }

    @Override
    public Page<IngestionDayStatusDTO> listIngestionDays(LocalDate arrivalFrom, LocalDate arrivalTo, String status, Pageable pageable) {
        validateArrivalRange(arrivalFrom, arrivalTo);
        validatePageable(pageable);
        List<IngestionDayStatusDTO> days = List.of(ingestionDay(ARRIVAL_DAY));
        List<IngestionDayStatusDTO> filtered = days.stream()
            .filter(day -> matchesDate(day.arrivalDate(), arrivalFrom, arrivalTo))
            .filter(day -> status == null || status.equals(day.status().name()))
            .toList();
        return toPage(filtered, pageable);
    }

    @Override
    public IngestionDayStatusDTO getIngestionDay(LocalDate arrivalDate) {
        if (!ARRIVAL_DAY.equals(arrivalDate)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Giornata di ingestion non trovata");
        }
        return ingestionDay(arrivalDate);
    }

    @Override
    public Page<IngestionEventDTO> searchIngestionEvents(
        LocalDate arrivalDate,
        UUID executionId,
        String level,
        String eventCode,
        Pageable pageable
    ) {
        validatePageable(pageable);
        List<IngestionEventDTO> events = List.of(ingestionEvent());
        List<IngestionEventDTO> filtered = events.stream()
            .filter(event -> arrivalDate == null || arrivalDate.equals(event.arrivalDate()))
            .filter(event -> executionId == null || executionId.equals(event.executionId()))
            .filter(event -> level == null || level.equals(event.level().name()))
            .filter(event -> eventCode == null || eventCode.equals(event.eventCode()))
            .toList();
        return toPage(filtered, pageable);
    }

    private FlowRevisionSummaryDTO flowSummary(
        UUID revisionId,
        String flowId,
        LocalDate arrivalDate,
        int ordinal,
        boolean latest,
        String dataArea,
        String creditorInstitutionId
    ) {
        return new FlowRevisionSummaryDTO(
            revisionId,
            flowId,
            "MOCK-UNIQUE-" + ordinal,
            arrivalDate.atTime(10 + ordinal, 30).toInstant(java.time.ZoneOffset.UTC),
            arrivalDate,
            ordinal,
            2,
            latest,
            arrivalDate.atTime(9, 0).toInstant(java.time.ZoneOffset.UTC),
            arrivalDate,
            "MOCK-PSP-001",
            "MOCK-INTERMEDIARY-001",
            1L,
            new BigDecimal("12.50"),
            FdrDataScope.valueOf(dataArea),
            creditorInstitutionId == null ? MOCK_CREDITOR_ID : creditorInstitutionId,
            arrivalDate
        );
    }

    private ReportedPaymentRowDTO paymentRow(
        UUID revisionId,
        String flowId,
        String iuv,
        String iur,
        BigDecimal amount,
        String creditorInstitutionId
    ) {
        LocalDate arrivalDate = revisionId.equals(REVISION_1) ? ARRIVAL_DAY : LATEST_ARRIVAL_DAY;
        return new ReportedPaymentRowDTO(
            revisionId.equals(REVISION_1) ? MOCK_HISTORICAL_PAYMENT_ID : MOCK_PAYMENT_ID,
            revisionId,
            flowId,
            revisionId.equals(REVISION_1) ? "MOCK-UNIQUE-1" : "MOCK-UNIQUE-2",
            iuv,
            iur,
            amount,
            "OK",
            arrivalDate.atTime(10, 35).toInstant(java.time.ZoneOffset.UTC),
            1,
            arrivalDate.atTime(9, 0).toInstant(java.time.ZoneOffset.UTC),
            "MOCK-PSP-001",
            "MOCK-INTERMEDIARY-001",
            arrivalDate.atTime(12, 30).toInstant(java.time.ZoneOffset.UTC),
            creditorInstitutionId == null ? MOCK_CREDITOR_ID : creditorInstitutionId,
            arrivalDate
        );
    }

    private void writeCsvRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            String value = values.get(index);
            writer.write('"');
            writer.write(value == null ? "" : value.replace("\"", "\"\""));
            writer.write('"');
        }
        writer.write("\r\n");
    }

    private ReingestionBatchDTO createBatch(UUID id, List<LocalDate> dates) {
        Instant now = Instant.now();
        List<ReingestionDayItemDTO> items = new ArrayList<>();
        for (int index = 0; index < dates.size(); index++) {
            items.add(new ReingestionDayItemDTO(dates.get(index), index + 1, FdrReingestionDayStatus.REQUESTED, null, null, null, null, null));
        }
        return new ReingestionBatchDTO(id, FdrAsyncStatus.ACCEPTED, now, null, null, items);
    }

    private ReingestionBatchDTO mockBatch() {
        return new ReingestionBatchDTO(
            MOCK_BATCH_ID,
            FdrAsyncStatus.COMPLETED,
            Instant.parse("2026-11-15T10:00:00Z"),
            Instant.parse("2026-11-15T10:01:00Z"),
            Instant.parse("2026-11-15T10:05:00Z"),
            List.of(new ReingestionDayItemDTO(ARRIVAL_DAY, 1, FdrReingestionDayStatus.COMPLETED, null, null, null, null, null))
        );
    }

    private IngestionDayStatusDTO ingestionDay(LocalDate arrivalDate) {
        return new IngestionDayStatusDTO(
            arrivalDate,
            FdrIngestionDayStatus.COMPLETED,
            MOCK_EXECUTION_ID,
            Instant.parse("2026-11-15T10:00:00Z"),
            Instant.parse("2026-11-15T10:05:00Z"),
            1,
            1,
            1L,
            1L,
            null,
            null,
            Instant.parse("2026-11-15T10:05:00Z")
        );
    }

    private IngestionEventDTO ingestionEvent() {
        return new IngestionEventDTO(
            1L,
            ARRIVAL_DAY,
            MOCK_EXECUTION_ID,
            Instant.parse("2026-11-15T10:05:00Z"),
            "INGESTION_COMPLETED",
            FdrEventLevel.INFO,
            FdrEventSourceCode.FLUSSI_RENDICONTAZIONE,
            "mock-fdr.csv",
            null,
            1L,
            1L,
            0L,
            250L,
            null,
            null,
            Map.of("mock", true)
        );
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        long offset = pageable.getOffset();
        if (offset >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }
        int start = (int) offset;
        int end = (int) Math.min(offset + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    private boolean matchesFilters(FlowRevisionSummaryDTO summary, LocalDate from, LocalDate to, String creditorInstitutionId) {
        return matchesDate(summary.arrivalDate(), from, to) &&
            (creditorInstitutionId == null || creditorInstitutionId.equals(summary.creditorInstitutionId()));
    }

    private boolean matchesDate(LocalDate date, LocalDate from, LocalDate to) {
        return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
    }

    private void validateArrivalRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > 30)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervallo arrivalFrom/arrivalTo non valido");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged() || pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginazione non valida");
        }
    }

    private void validateScope(String scope) {
        if (!"ONLINE".equals(scope) && !"HISTORICAL".equals(scope) && !"ALL".equals(scope)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope non valido");
        }
    }

    private void validateMatchMode(String matchMode) {
        if (!"EXACT".equals(matchMode) && !"PREFIX".equals(matchMode) && !"CONTAINS".equals(matchMode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "matchMode non valido");
        }
    }

    private void validateSearchValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " è obbligatorio");
        }
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.length() < 8 || key.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key non valida");
        }
    }

    private void validateReingestion(List<LocalDate> dates, String reason) {
        if (dates == null || dates.isEmpty() || dates.size() > 31 || dates.stream().distinct().count() != dates.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "arrivalDates deve contenere da 1 a 31 date univoche");
        }
        if (reason == null || reason.trim().length() < 5 || reason.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason deve contenere da 5 a 1000 caratteri");
        }
    }

    private record StoredBatch(String fingerprint, ReingestionBatchDTO batch) {}

    private record RevisionMock(UUID revisionId, LocalDate arrivalDate, int ordinal, boolean latest, String dataArea) {}
}
