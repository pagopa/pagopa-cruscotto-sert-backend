package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.asString;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.parseInfoMatch;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.tokenAsHex;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toDouble;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstant;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstantFromDate;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for SERT APIs with Mock Data.
 */
@Service
public class SertServiceImpl implements SertService {

    private final Logger log = LoggerFactory.getLogger(SertServiceImpl.class);

    private final PositionRepository positionRepository;

    public SertServiceImpl(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }


    @Override
    public Page<PositionPaymentExtraDTO> searchByNav(String nav, String pa, Pageable pageable) {
        log.debug("Request to search by NAV: {}, PA: {}, offset: {}, limit: {}", nav, pa, pageable.getOffset(), pageable.getPageSize());

        Page<Position> positionsPage = positionRepository.findByNavOrPa(nav, pa, pageable);
        if (positionsPage == null || positionsPage.isEmpty()) {
            return Page.empty(pageable);
        }
        return positionsPage.map(pos -> PositionPaymentExtraDTO.builder()
            .nav(pos.getNav())
            .paEmittente(pos.getPaEmittente())
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByIuv(String pa, String nav, String iuv,Pageable pageable) {
        log.debug("Request to search by IUV: {}, PA: {}, NAV: {}, offset: {}, limit: {}", iuv, pa, nav, pageable.getOffset(), pageable.getPageSize());

        Page<Position> positionsPage = positionRepository.findByIuvAndOptionalNavAndPa(iuv, nav, pa, pageable);

        return positionsPage.map(pos -> PositionPaymentExtraDTO.builder()
            .nav(pos.getNav())
            .paEmittente(pos.getPaEmittente())
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByCart(String pa, String nav, String idCart, Pageable pageable) {
        log.debug("Request to search by Cart: {}, offset: {}, limit: {}", idCart, pageable.getOffset(), pageable.getPageSize());

        Page<Position> positionsPage = positionRepository.findByCartAndOptionalNavAndPa(idCart, nav, pa, pageable);

        return positionsPage.map(pos -> PositionPaymentExtraDTO.builder()
            .nav(pos.getNav())
            .paEmittente(pos.getPaEmittente())
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByToken( String pa, String nav,String token, Pageable pageable) {
        log.debug("Request to search by Token: {}, offset: {}, limit: {}", token, pageable.getOffset(), pageable.getPageSize());

        if (token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String normalizedToken = token.trim();

        Page<Position> positionsPage = positionRepository.findByTokenAndOptionalNavAndPa(normalizedToken, nav, pa, pageable);

        return positionsPage.map(pos -> PositionPaymentExtraDTO.builder()
            .nav(pos.getNav())
            .paEmittente(pos.getPaEmittente())
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchExtra(String pa, String nav, String searchValue, Pageable pageable) {
        log.debug("Request to search extra: {}, offset: {}, limit: {}", searchValue, pageable.getOffset(), pageable.getPageSize());

        if (searchValue == null) {
            return Page.empty(pageable);
        }


        Page<Object[]> groupedRowsPage = positionRepository.findGroupedByExtraValueAndOptionalNavAndPa(
            searchValue,
            nav,
            pa,
            pageable
        );

        return groupedRowsPage.map(row -> PositionPaymentExtraDTO.builder()
            .nav((String) row[0])
            .paEmittente((String) row[1])
            .match(parseInfoMatch(row[2]))
            .build());
    }

    @Override
    public Page<PositionPaymentDTO> getPosition(String nav, String paEmittente, Pageable pageable) {
        log.debug("Request to get position: {}, {}", nav, paEmittente);

        Page<Object[]> rows = positionRepository.findPositionDetailRows(nav, paEmittente, pageable);
        if (rows == null || rows.isEmpty()) {
            return Page.empty(pageable);
        }

        Object[] firstRow = rows.getContent().get(0);
        Object[] preferredRow = rows.stream().filter(row -> row[5] != null).findFirst().orElse(firstRow);
        Object[] payedRow = rows.stream().filter(row -> row[7] != null && row[5] != null).findFirst().orElse(null);

        List<String> allTokens = rows.stream()
            .map(row -> tokenAsHex(row[5]))
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        long okTokenCount = positionRepository.countOkTokensByNavAndPa(nav, paEmittente);
        boolean multiOutcome = okTokenCount > 1;

        PositionPaymentDTO dto = PositionPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(firstRow[0]))
                .paEmittente(asString(firstRow[1]))
                .iuv(asString(preferredRow[3]))
                .creditorReferenceId(asString(preferredRow[4]))
                .lastEvent(toInstant(firstRow[2]))
                .isCached(false)
                .build())
            .tokens(allTokens.size())
            .allTokens(allTokens)
            .payed(
                payedRow == null
                    ? null
                    : PayedDTO.builder()
                        .token(tokenAsHex(payedRow[5]))
                        .paymentBorn(toInstantFromDate(payedRow[6]))
                        .payedDate(toInstant(payedRow[7]))
                        .multiOutcome(multiOutcome)
                        .build()
            )
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
                .isCart(rows.stream().anyMatch(r -> r[18] != null))  // true se almeno un token ha idCarrello != null
                .isGpd(false)
                .isStandin(false)
                .isDw(nav.startsWith("351"))
                .build())
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, allTokens.size());
    }


    @Override
    public TokenInfoDTO getTokenInfo(String token) {
        log.debug("Request to get token info: {}", token);

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        List<Object[]> rows = positionRepository.findTokenDetailRow(token.trim());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] row = rows.get(0);
        boolean isPayed = row[7] != null;

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        String rowNav = asString(row[0]);
        String rowPa  = asString(row[1]);
        boolean multiOutcome = false;
        if (rowNav != null && rowPa != null) {
            long okTokenCount = positionRepository.countOkTokensByNavAndPa(rowNav, rowPa);
            multiOutcome = okTokenCount > 1;
        }

        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(rowNav)
                .paEmittente(rowPa)
                .iuv(asString(row[3]))
                .creditorReferenceId(asString(row[4]))
                .lastEvent(toInstant(row[2]))
                .isCached(false)
                .build())
            .isPayedToken(isPayed)
            .payed(
                !isPayed
                    ? null
                    : PayedDTO.builder()
                        .token(tokenAsHex(row[5]))
                        .paymentBorn(toInstantFromDate(row[6]))
                        .payedDate(toInstant(row[7]))
                        .multiOutcome(multiOutcome)
                        .build()
            )
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
                .isCart(row[18] != null)  // true se idCarrello != null
                .isGpd(false)
                .isStandin(false)
                .isDw(rowNav != null && rowNav.startsWith("351"))
                .build())
            .build();
    }

    @Override
    public Page<TransferPaymentDTO> getTransfers(String nav, String paEmittente, String token, Pageable pageable) {
        log.debug("Request to get transfers for: {}, {}, {}, {}, {}", nav, paEmittente, token,pageable.getOffset(), pageable.getPageSize() );

        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return null;
        }

        Page<Object[]> rows = positionRepository.findTransferDetailRows(nav, paEmittente, token.trim(), pageable);
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] latestRow = rows.getContent().get(0);

        List<TransferObjectDTO> transfers = rows.stream()
            .map(this::toTransferObject)
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

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, rows.getTotalElements());
    }

    private TransferObjectDTO toTransferObject(Object[] row) {
        return TransferObjectDTO.builder()
            .idTransfer(row[7] == null ? null : Integer.valueOf(asString(row[7])))
            .typeTransfer(Boolean.TRUE.equals(row[8]) ? "bollo" : "sepa")
            .iban(asString(row[9]))
            .amount(toDouble(row[10]))
            .paFiscalCode(asString(row[11]))
            .build();
    }

    @Override
    public Page<WorkflowResponseDTO> getWorkflows(String nav, String paEmittente, Pageable pageable) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);

        Page<Object[]> workflowEvents = positionRepository.findPositionWorkflows(nav, paEmittente, pageable);

        if (workflowEvents == null || workflowEvents.isEmpty()) {
            return null;
        }

        List<WorkflowObjectDTO> eventsPositionList = new java.util.ArrayList<>();
        List<WorkflowTokenObjectDTO> eventsTokenList = new java.util.ArrayList<>();

        List<Object[]> rows = workflowEvents.getContent();
        int offset = (int) pageable.getOffset();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            // In case of a LEFT JOIN where there are no events at all, all event fields would be null.
            if (row[0] == null && row[4] == null) {
                continue;
            }
            int positionNumber = offset + i + 1;
            String token = tokenAsHex(row[6]);

            if (token != null) {
                WorkflowTokenObjectDTO tokenDTO = WorkflowTokenObjectDTO.builder()
                    .insertedtimestamp(toInstant(row[0]))
                    .tipoevento(asString(row[1]))
                    .sottotipoevento(asString(row[2]))
                    .outcome(asString(row[3]))
                    .eventId(asString(row[4]))
                    .faultcode(row[5] != null ? String.valueOf(row[5]) : null)
                    .positionNumber(positionNumber)
                    .token(token)
                    .build();
                eventsTokenList.add(tokenDTO);
            } else {
                WorkflowObjectDTO positionDTO = WorkflowObjectDTO.builder()
                    .insertedtimestamp(toInstant(row[0]))
                    .tipoevento(asString(row[1]))
                    .sottotipoevento(asString(row[2]))
                    .outcome(asString(row[3]))
                    .eventId(asString(row[4]))
                    .faultcode(row[5] != null ? String.valueOf(row[5]) : null)
                    .positionNumber(positionNumber)
                    .build();
                eventsPositionList.add(positionDTO);
            }
        }

        Long totalCount = workflowEvents.getTotalElements();

        WorkflowResponseDTO dto = WorkflowResponseDTO.builder()
            .count(totalCount)
            .eventsPosition(eventsPositionList)
            .eventsToken(eventsTokenList)
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, totalCount);
    }

    @Override
    public Page<ExtraInfoResponseDTO> getExtraInfo(String token, Pageable pageable) {
        log.debug("Request to get extra info for token: {}", token);

        Page<Object[]> rows = positionRepository.findExtraInfoByToken(token.trim(),pageable);
        if (rows == null || rows.isEmpty()) {
            return null;
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
        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, rows.getTotalElements());
    }
}
