package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionDetailDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FlowRevisionSummaryDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.FdrSearchedBy;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionDayStatusDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.IngestionEventDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.OccurrenceResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReingestionBatchDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.fdr.ReportedPaymentRowDTO;
import java.time.LocalDate;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FdrService {

    Page<FlowRevisionSummaryDTO> searchFlows(
        String flowId,
        LocalDate arrivalFrom,
        LocalDate arrivalTo,
        String scope,
        String creditorInstitutionId,
        Pageable pageable
    );

    FlowRevisionDetailDTO getRevision(UUID revisionId);

    Page<ReportedPaymentRowDTO> searchPayments(
        UUID revisionId,
        String iuv,
        String iur,
        BigDecimal amount,
        String creditorInstitutionId,
        String matchMode,
        Pageable pageable
    );

    Page<OccurrenceResultDTO> searchOccurrences(
        FdrSearchedBy searchedBy,
        String searchedValue,
        String flowId,
        String creditorInstitutionId,
        LocalDate arrivalFrom,
        LocalDate arrivalTo,
        String scope,
        Pageable pageable
    );

    void exportRevisionCsv(UUID revisionId, OutputStream outputStream) throws IOException;

    ReingestionBatchDTO createReingestion(String idempotencyKey, List<LocalDate> arrivalDates, String reason);

    Page<ReingestionBatchDTO> listReingestions(String status, Pageable pageable);

    ReingestionBatchDTO getReingestion(UUID requestId);

    Page<IngestionDayStatusDTO> listIngestionDays(LocalDate arrivalFrom, LocalDate arrivalTo, String status, Pageable pageable);

    IngestionDayStatusDTO getIngestionDay(LocalDate arrivalDate);

    Page<IngestionEventDTO> searchIngestionEvents(LocalDate arrivalDate, UUID executionId, String level, String eventCode, Pageable pageable);
}
