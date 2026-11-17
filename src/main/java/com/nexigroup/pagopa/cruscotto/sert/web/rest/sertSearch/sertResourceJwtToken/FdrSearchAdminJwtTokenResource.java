package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken;

import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.FdrService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionDetailDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionSummaryDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrSearchedBy;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionDayStatusDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionEventDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.OccurrenceResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReingestionBatchDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReportedPaymentRowDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@Validated
@RestController
@RequestMapping(value = "/api")
public class FdrSearchAdminJwtTokenResource {

    private final FdrService fdrService;

    public FdrSearchAdminJwtTokenResource(FdrService fdrService) {
        this.fdrService = fdrService;
    }

    @GetMapping("/fdr/flows")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_SEARCH_FLOWS + "\")")
    public ResponseEntity<List<FlowRevisionSummaryDTO>> searchFlows(
        @RequestParam String flowId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalTo,
        @RequestParam(defaultValue = "ONLINE") String scope,
        @RequestParam(required = false) String creditorInstitutionId,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(fdrService.searchFlows(flowId, arrivalFrom, arrivalTo, scope, creditorInstitutionId, pageable));
    }

    @GetMapping("/fdr/flow-revisions/{revisionId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_GET_FLOW_REVISION + "\")")
    public ResponseEntity<FlowRevisionDetailDTO> getRevision(@PathVariable UUID revisionId) {
        return ResponseEntity.ok(fdrService.getRevision(revisionId));
    }

    @GetMapping("/fdr/flow-revisions/{revisionId}/payments")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_SEARCH_REVISION_PAYMENTS + "\")")
    public ResponseEntity<List<ReportedPaymentRowDTO>> searchPayments(
        @PathVariable UUID revisionId,
        @RequestParam(required = false) String iuv,
        @RequestParam(required = false) String iur,
        @RequestParam(required = false) BigDecimal amount,
        @RequestParam(required = false) String creditorInstitutionId,
        @RequestParam(defaultValue = "EXACT") String matchMode,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(fdrService.searchPayments(revisionId, iuv, iur, amount, creditorInstitutionId, matchMode, pageable));
    }

    @GetMapping("/fdr/occurrences/by-iuv")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_SEARCH_OCCURRENCES_BY_IUV + "\")")
    public ResponseEntity<List<OccurrenceResultDTO>> searchByIuv(
        @RequestParam String iuv,
        @RequestParam(required = false) @Size(max = 30) String flowId,
        @RequestParam(required = false) String creditorInstitutionId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalTo,
        @RequestParam(defaultValue = "ONLINE") String scope,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        FdrSearchedBy searchedBy = flowId != null
            ? FdrSearchedBy.IUV_AND_FLOW_ID
            : creditorInstitutionId != null ? FdrSearchedBy.IUV_AND_CREDITOR_INSTITUTION : FdrSearchedBy.IUV;
        return paginatedResponse(fdrService.searchOccurrences(searchedBy, iuv, flowId, creditorInstitutionId, arrivalFrom, arrivalTo, scope, pageable));
    }

    @GetMapping("/fdr/occurrences/by-iur")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_SEARCH_OCCURRENCES_BY_IUR + "\")")
    public ResponseEntity<List<OccurrenceResultDTO>> searchByIur(
        @RequestParam String iur,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalTo,
        @RequestParam(defaultValue = "ONLINE") String scope,
        @RequestParam(required = false) String creditorInstitutionId,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(
            fdrService.searchOccurrences(FdrSearchedBy.IUR, iur, null, creditorInstitutionId, arrivalFrom, arrivalTo, scope, pageable)
        );
    }

    @PostMapping(value = "/admin/fdr/reingestions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_REQUEST_REINGESTION + "\")")
    public ResponseEntity<ReingestionBatchDTO> requestReingestion(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody ReingestionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(fdrService.createReingestion(idempotencyKey, request.arrivalDates(), request.reason()));
    }

    @GetMapping("/admin/fdr/reingestions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_LIST_REINGESTIONS + "\")")
    public ResponseEntity<List<ReingestionBatchDTO>> listReingestions(
        @RequestParam(required = false) String status,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(fdrService.listReingestions(status, pageable));
    }

    @GetMapping("/admin/fdr/reingestions/{requestId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_GET_REINGESTION + "\")")
    public ResponseEntity<ReingestionBatchDTO> getReingestion(@PathVariable UUID requestId) {
        return ResponseEntity.ok(fdrService.getReingestion(requestId));
    }

    @GetMapping("/admin/fdr/ingestion-days")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_LIST_INGESTION_DAYS + "\")")
    public ResponseEntity<List<IngestionDayStatusDTO>> listIngestionDays(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalTo,
        @RequestParam(required = false) String status,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(fdrService.listIngestionDays(arrivalFrom, arrivalTo, status, pageable));
    }

    @GetMapping("/admin/fdr/ingestion-days/{arrivalDate}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_GET_INGESTION_DAY + "\")")
    public ResponseEntity<IngestionDayStatusDTO> getIngestionDay(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate
    ) {
        return ResponseEntity.ok(fdrService.getIngestionDay(arrivalDate));
    }

    @GetMapping("/admin/fdr/ingestion-events")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_SEARCH_INGESTION_EVENTS + "\")")
    public ResponseEntity<List<IngestionEventDTO>> searchIngestionEvents(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate,
        @RequestParam(required = false) UUID executionId,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String eventCode,
        @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return paginatedResponse(fdrService.searchIngestionEvents(arrivalDate, executionId, level, eventCode, pageable));
    }

    @GetMapping(value = "/fdr/flow-revisions/{revisionId}/export", produces = "text/csv")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FDR_EXPORT_FLOW_REVISION + "\")")
    public ResponseEntity<StreamingResponseBody> exportRevision(@PathVariable UUID revisionId) {
        StreamingResponseBody body = outputStream -> fdrService.exportRevisionCsv(revisionId, outputStream);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fdr-revision-" + revisionId + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(body);
    }

    public record ReingestionRequest(
        @NotEmpty @Size(max = 31) List<@NotNull LocalDate> arrivalDates,
        @NotBlank @Size(min = 5, max = 1000) String reason
    ) {}

    private <T> ResponseEntity<List<T>> paginatedResponse(Page<T> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
