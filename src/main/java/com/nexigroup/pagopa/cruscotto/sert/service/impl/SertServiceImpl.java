package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.asString;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.parseInfoMatch;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toDouble;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstant;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstantFromDate;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public UnifiedSearchResponseDTO searchByNav(String nav, String pa) {
        log.debug("Request to search by NAV: {}, PA: {}", nav, pa);

        List<Position> positions = positionRepository.findByNavOrPa(nav, pa);
        if (positions == null || positions.isEmpty()) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<PositionPaymentExtraDTO> results = positions.stream()
            .map(pos -> PositionPaymentExtraDTO.builder()
                .nav(pos.getNav())
                .paEmittente(pos.getPaEmittente())
                .build())
            .collect(Collectors.toList());

        return UnifiedSearchResponseDTO.builder()
            .results(results)
            .count(results.size())
            .build();
    }

    @Override
    public UnifiedSearchResponseDTO searchByIuv(String pa, String nav, String iuv) {
        log.debug("Request to search by IUV: {}, PA: {}, NAV: {}", iuv, pa, nav);
        List<Position> positions = positionRepository.findByIuvAndOptionalNavAndPa(iuv, nav, pa);
        
        if (positions == null || positions.isEmpty()) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<PositionPaymentExtraDTO> results = positions.stream()
            .map(pos -> PositionPaymentExtraDTO.builder()
                .nav(pos.getNav())
                .paEmittente(pos.getPaEmittente())
                .build())
            .collect(Collectors.toList());

        return UnifiedSearchResponseDTO.builder()
            .results(results)
            .count(results.size())
            .build();
    }

    @Override
    public UnifiedSearchResponseDTO searchByCart(String pa, String nav, String idCart) {
        log.debug("Request to search by Cart: {}", idCart);
        List<Position> positions = positionRepository.findByCartAndOptionalNavAndPa(idCart, nav, pa);

        if (positions == null || positions.isEmpty()) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<PositionPaymentExtraDTO> results = positions.stream()
            .map(pos -> PositionPaymentExtraDTO.builder()
                .nav(pos.getNav())
                .paEmittente(pos.getPaEmittente())
                .build())
            .collect(Collectors.toList());

        return UnifiedSearchResponseDTO.builder()
            .results(results)
            .count(results.size())
            .build();
    }

    @Override
    public UnifiedSearchResponseDTO searchByToken( String pa, String nav,String token) {
        log.debug("Request to search by Token: {}", token);
        List<Position> positions = positionRepository.findByTokenAndOptionalNavAndPa(token, nav, pa);

        if (positions == null || positions.isEmpty()) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<PositionPaymentExtraDTO> results = positions.stream()
            .map(pos -> PositionPaymentExtraDTO.builder()
                .nav(pos.getNav())
                .paEmittente(pos.getPaEmittente())
                .build())
            .collect(Collectors.toList());

        return UnifiedSearchResponseDTO.builder()
            .results(results)
            .count(results.size())
            .build();
    }

    @Override
    public UnifiedSearchResponseDTO searchExtra(String pa, String nav, String searchValue) {
        log.debug("Request to search extra: {}", searchValue);

        if (searchValue == null) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<Object[]> groupedRows = positionRepository.findGroupedByExtraValueAndOptionalNavAndPa(
            searchValue,
            nav,
            pa
        );

        if (groupedRows == null || groupedRows.isEmpty()) {
            return UnifiedSearchResponseDTO.builder()
                .results(Collections.emptyList())
                .count(0)
                .build();
        }

        List<PositionPaymentExtraDTO> results = groupedRows.stream()
            .map(row -> PositionPaymentExtraDTO.builder()
                .nav((String) row[0])
                .paEmittente((String) row[1])
                .match(parseInfoMatch(row[2]))
                .build())
            .collect(Collectors.toList());

        return UnifiedSearchResponseDTO.builder()
            .results(results)
            .count(results.size())
            .build();
    }

    @Override
    public PositionPaymentDTO getPosition(String nav, String paEmittente) {
        log.debug("Request to get position: {}, {}", nav, paEmittente);

        List<Object[]> rows = positionRepository.findPositionDetailRows(nav, paEmittente);
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] firstRow = rows.get(0);
        Object[] preferredRow = rows.stream().filter(row -> row[5] != null).findFirst().orElse(firstRow);
        Object[] payedRow = rows.stream().filter(row -> row[7] != null && row[5] != null).findFirst().orElse(null);

        List<String> allTokens = rows.stream()
            .map(row -> asString(row[5]))
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));

        long distinctOutcomes = rows.stream().map(row -> asString(row[8])).filter(Objects::nonNull).distinct().count();

        return PositionPaymentDTO.builder()
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
                        .token(asString(payedRow[5]))
                        .paymentBorn(toInstantFromDate(payedRow[6]))
                        .payedDate(toInstant(payedRow[7]))
                        .multiOutcome(distinctOutcomes > 1)
                        .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(preferredRow[11]))
                .ptPa(asString(preferredRow[12]))
                .ptPsp(asString(preferredRow[13]))
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
                .isCart(preferredRow[18] != null)
                .build())
            .build();
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

        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(row[0]))
                .paEmittente(asString(row[1]))
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
                        .token(asString(row[5]))
                        .paymentBorn(toInstantFromDate(row[6]))
                        .payedDate(toInstant(row[7]))
                        .multiOutcome(false)
                        .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(row[11]))
                .ptPa(asString(row[12]))
                .ptPsp(asString(row[13]))
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
                .build())
            .build();
    }

    @Override
    public TransferPaymentDTO getTransfers(String nav, String paEmittente, String token) {
        log.debug("Request to get transfers for: {}, {}, {}", nav, paEmittente, token);

        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return null;
        }

        List<Object[]> rows = positionRepository.findTransferDetailRows(nav, paEmittente, token.trim());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] latestRow = rows.get(0);

        return TransferPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(latestRow[0]))
                .paEmittente(asString(latestRow[1]))
                .iuv(asString(latestRow[3]))
                .creditorReferenceId(asString(latestRow[4]))
                .lastEvent(toInstant(latestRow[2]))
                .isCached(false)
                .build())
            .token(asString(latestRow[5]))
            .transfersCount(toDouble(latestRow[6]))
            .transfers(TransferObjectDTO.builder()
                .idTransfer(latestRow[7] == null ? null : Integer.valueOf(asString(latestRow[7])))
                .typeTransfer(Boolean.TRUE.equals(latestRow[8]) ? "bollo" : "sepa")
                .iban(asString(latestRow[9]))
                .amount(toDouble(latestRow[10]))
                .paFiscalCode(asString(latestRow[11]))
                .build())
            .build();
    }

    @Override
    public WorkflowResponseDTO getWorkflows(String nav, String paEmittente) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);

        if (nav == null || paEmittente == null) {
            return WorkflowResponseDTO.builder()
                .count(0.0)
                .eventsPosition(Collections.emptyList())
                .eventsToken(Collections.emptyList())
                .build();
        }

        List<Object[]> positionEvents = positionRepository.findEventsPositionByNavAndPa(nav, paEmittente);
        List<Object[]> tokenEvents = positionRepository.findEventsTokenByNavAndPa(nav, paEmittente);

        List<WorkflowObjectDTO> eventsPositionList = positionEvents != null && !positionEvents.isEmpty()
            ? positionEvents.stream()
                .map(row -> WorkflowObjectDTO.builder()
                    .insertedtimestamp(toInstant(row[0]))
                    .tipoevento(asString(row[1]))
                    .outcome(asString(row[2]))
                    .eventId(asString(row[3]))
                    .faultcode(row[4] != null ? String.valueOf(row[4]) : null)
                    .build())
                .collect(Collectors.toList())
            : Collections.emptyList();

        List<WorkflowTokenObjectDTO> eventsTokenList = tokenEvents != null && !tokenEvents.isEmpty()
            ? tokenEvents.stream()
                .map(row -> WorkflowTokenObjectDTO.builder()
                    .insertedtimestamp(toInstant(row[0]))
                    .tipoevento(asString(row[1]))
                    .outcome(asString(row[2]))
                    .eventId(asString(row[3]))
                    .faultcode(row[4] != null ? String.valueOf(row[4]) : null)
                    .token(asString(row[5]))
                    .build())
                .collect(Collectors.toList())
            : Collections.emptyList();

        double totalCount = eventsPositionList.size() + eventsTokenList.size();

        return WorkflowResponseDTO.builder()
            .count(totalCount)
            .eventsPosition(eventsPositionList)
            .eventsToken(eventsTokenList)
            .build();
    }

    @Override
    public ExtraInfoResponseDTO getExtraInfo(String token) {
        log.debug("Request to get extra info for token: {}", token);

        if (token == null || token.trim().isEmpty()) {
            return ExtraInfoResponseDTO.builder()
                .count(0)
                .results(Collections.emptyList())
                .build();
        }

        List<Object[]> rows = positionRepository.findExtraInfoByToken(token.trim());
        if (rows == null || rows.isEmpty()) {
            return ExtraInfoResponseDTO.builder()
                .count(0)
                .results(Collections.emptyList())
                .build();
        }

        List<ExtraInfoObjectDTO> results = rows.stream()
            .filter(row -> row[3] != null)
            .map(row -> ExtraInfoObjectDTO.builder()
                .nav(asString(row[0]))
                .paEmittente(asString(row[1]))
                .token(asString(row[2]))
                .name(asString(row[3]))
                .value(asString(row[4]))
                .tipoevento(asString(row[5]))
                .build())
            .collect(Collectors.toList());

        return ExtraInfoResponseDTO.builder()
            .count(results.size())
            .results(results)
            .build();
    }
}
