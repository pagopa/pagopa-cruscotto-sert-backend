package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.asString;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toDouble;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstant;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstantFromDate;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.tokenAsHex;

import com.nexigroup.pagopa.cruscotto.sert.repository.freeze.FreezePositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertFreezeDataService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SertFreezeDataServiceImpl implements SertFreezeDataService {

    private final FreezePositionRepository freezePositionRepository;

    public SertFreezeDataServiceImpl(FreezePositionRepository freezePositionRepository) {
        this.freezePositionRepository = freezePositionRepository;
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchFromHistory(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCart,
        String info,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    ) {
        return freezePositionRepository.search(pa, nav, iuv, token, idCart, info, dateFrom, dateTo, pageable);
    }

    @Override
    public Page<PositionPaymentDTO> getPositionFromHistory(String nav, String paEmittente, Pageable pageable) {
        Page<Object[]> rows = freezePositionRepository.findPositionDetailRows(nav, paEmittente, pageable);
        if (rows.isEmpty()) {
            return Page.empty(pageable);
        }
        Object[] firstRow = rows.getContent().get(0);
        Object[] preferredRow = rows.stream().filter(row -> row[5] != null).findFirst().orElse(firstRow);
        Object[] payedRow = rows.stream().filter(row -> row[7] != null && row[5] != null).findFirst().orElse(null);
        List<String> allTokens = rows.stream()
            .map(row -> tokenAsHex(row[5]))
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));
        boolean multiOutcome = freezePositionRepository.countOkTokensByNavAndPa(nav, paEmittente) > 1;

        PositionPaymentDTO dto = PositionPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(firstRow[0]))
                .paEmittente(asString(firstRow[1]))
                .iuv(asString(preferredRow[3]))
                .creditorReferenceId(asString(preferredRow[4]))
                .lastEvent(toInstant(firstRow[2]))
                .isCached(false)
                .build())
            .tokens(allTokens.isEmpty() ? 0L : rows.getTotalElements())
            .allTokens(allTokens)
            .payed(payedRow == null ? null : PayedDTO.builder()
                .token(tokenAsHex(payedRow[5]))
                .paymentBorn(toInstantFromDate(payedRow[6]))
                .payedDate(toInstant(payedRow[7]))
                .multiOutcome(multiOutcome)
                .build())
            .actors(ActorsDTO.builder()
                .psp(asString(preferredRow[11]))
                .pspDescription(asString(preferredRow[19]))
                .ptPa(asString(preferredRow[12]))
                .ptPaDescription(asString(preferredRow[20]))
                .ptPsp(asString(preferredRow[13]))
                .ptPspDescription(asString(preferredRow[21]))
                .station(asString(preferredRow[14]))
                .channel(asString(preferredRow[15]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(preferredRow[9]))
                .fee(toDouble(preferredRow[10]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(preferredRow[16]))
                .paymentMethod(asString(preferredRow[17]))
                .isCart(rows.stream().anyMatch(row -> row[18] != null))
                .isGpd(false)
                .isStandin(false)
                .isDw(nav.startsWith("351"))
                .build())
            .build();
        return new PageCustomImpl<>(
            List.of(dto),
            pageable,
            allTokens.isEmpty() ? 0L : rows.getTotalElements()
        );
    }

    @Override
    public TokenInfoDTO getTokenInfoFromHistory(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        List<Object[]> rows = freezePositionRepository.findTokenDetailRows(token.trim());
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = rows.get(0);
        String nav = asString(row[0]);
        String paEmittente = asString(row[1]);
        boolean isPayed = row[8] != null && "OK".equalsIgnoreCase(row[8].toString());
        boolean multiOutcome = nav != null && paEmittente != null &&
            freezePositionRepository.countOkTokensByNavAndPa(nav, paEmittente) > 1;

        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(nav)
                .paEmittente(paEmittente)
                .iuv(asString(row[3]))
                .creditorReferenceId(asString(row[4]))
                .lastEvent(toInstant(row[2]))
                .isCached(false)
                .build())
            .isPayedToken(isPayed)
            .payed(!isPayed ? null : PayedDTO.builder()
                .token(tokenAsHex(row[5]))
                .paymentBorn(toInstantFromDate(row[6]))
                .payedDate(toInstant(row[7]))
                .multiOutcome(multiOutcome)
                .build())
            .actors(ActorsDTO.builder()
                .psp(asString(row[11]))
                .pspDescription(asString(row[19]))
                .ptPa(asString(row[12]))
                .ptPaDescription(asString(row[20]))
                .ptPsp(asString(row[13]))
                .ptPspDescription(asString(row[21]))
                .station(asString(row[14]))
                .channel(asString(row[15]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(row[9]))
                .fee(toDouble(row[10]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(row[16]))
                .paymentMethod(asString(row[17]))
                .isCart(row[18] != null)
                .isGpd(false)
                .isStandin(false)
                .isDw(nav != null && nav.startsWith("351"))
                .build())
            .build();
    }

    @Override
    public Page<TransferPaymentDTO> getTransfersFromHistory(
        String nav,
        String paEmittente,
        String token,
        Pageable pageable
    ) {
        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Object[]> rows = freezePositionRepository.findTransferDetailRows(nav, paEmittente, token.trim(), pageable);
        if (rows.isEmpty()) {
            return Page.empty(pageable);
        }
        Object[] latestRow = rows.getContent().get(0);
        List<TransferObjectDTO> transfers = rows.stream()
            .map(row -> TransferObjectDTO.builder()
                .idTransfer(row[7] == null ? null : Integer.valueOf(asString(row[7])))
                .typeTransfer(Boolean.TRUE.equals(row[8]) ? "bollo" : "sepa")
                .iban(asString(row[9]))
                .amount(toDouble(row[10]))
                .paFiscalCode(asString(row[11]))
                .build())
            .collect(Collectors.toList());

        TransferPaymentDTO dto = TransferPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(latestRow[0]))
                .paEmittente(asString(latestRow[1]))
                .iuv(asString(latestRow[3]))
                .creditorReferenceId(asString(latestRow[4]))
                .lastEvent(toInstant(latestRow[2]))
                .isCached(false)
                .build())
            .token(tokenAsHex(latestRow[5]))
            .transfersCount(toDouble(latestRow[6]))
            .transfers(transfers)
            .build();
        return new PageCustomImpl<>(List.of(dto), pageable, rows.getTotalElements());
    }

    @Override
    public Page<WorkflowResponseDTO> getWorkflowsFromHistory(String nav, String paEmittente, Pageable pageable) {
        Page<Object[]> workflowEvents = freezePositionRepository.findPositionWorkflows(nav, paEmittente, pageable);
        if (workflowEvents.isEmpty()) {
            return Page.empty(pageable);
        }
        List<WorkflowObjectDTO> events = new ArrayList<>();
        for (Object[] row : workflowEvents) {
            if (row[0] == null && row[4] == null) {
                continue;
            }
            String faultcode = row[5] == null ? null : String.valueOf(row[5]);
            String subType = asString(row[2]);
            String reqResp = asString(row[7]);
            if ("REQ/RESP".equals(subType) && "REQ".equals(reqResp)) {
                faultcode = null;
            }
            events.add(WorkflowTokenObjectDTO.builder()
                .insertedtimestamp(toInstant(row[0]))
                .tipoevento(asString(row[1]))
                .sottotipoevento("REQ/RESP".equals(subType) ? reqResp : subType)
                .outcome(asString(row[3]))
                .eventId(asString(row[4]))
                .faultcode(faultcode)
                .token(tokenAsHex(row[6]))
                .id(asString(row[8]))
                .build());
        }
        WorkflowResponseDTO dto = WorkflowResponseDTO.builder().eventsPosition(events).build();
        return new PageCustomImpl<>(List.of(dto), pageable, workflowEvents.getTotalElements());
    }

    @Override
    public Page<ExtraInfoResponseDTO> getExtraInfoFromHistory(String token, Pageable pageable) {
        if (token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Object[]> rows = freezePositionRepository.findExtraInfoByToken(token.trim(), pageable);
        if (rows.isEmpty()) {
            return Page.empty(pageable);
        }
        List<ExtraInfoObjectDTO> results = rows.stream()
            .filter(row -> row[3] != null)
            .map(row -> ExtraInfoObjectDTO.builder()
                .nav(asString(row[0]))
                .paEmittente(asString(row[1]))
                .token(tokenAsHex(row[2]))
                .name(asString(row[3]))
                .value(asString(row[4]))
                .tipoevento(asString(row[5]))
                .build())
            .collect(Collectors.toList());
        ExtraInfoResponseDTO dto = ExtraInfoResponseDTO.builder()
            .count(rows.getTotalElements())
            .results(results)
            .build();
        return new PageCustomImpl<>(List.of(dto), pageable, rows.getTotalElements());
    }
}
