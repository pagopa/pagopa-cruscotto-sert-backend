package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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


    private List<String> parseInfoMatch(Object aggregatedInfoNames) {
        if (aggregatedInfoNames == null) {
            return Collections.emptyList();
        }
        String value = aggregatedInfoNames.toString().trim();
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public PositionPaymentDTO getPosition(String nav, String paEmittente) {
        log.debug("Request to get position: {}, {}", nav, paEmittente);
        return PositionPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(nav)
                .paEmittente(paEmittente)
                .iuv("iuv123")
                .creditorReferenceId("refId123")
                .lastEvent(Instant.now())
                .isCached(true)
                .build())
            .tokens(1)
            .allTokens(Collections.singletonList("abcde12345abcde12345abcde1234512"))
            .payed(PayedDTO.builder()
                .token("abcde12345abcde12345abcde1234512")
                .paymentBorn(Instant.now().minusSeconds(3600))
                .payedDate(Instant.now())
                .multiOutcome(false)
                .build())
            .actors(ActorsDTO.builder()
                .psp("PSP Test")
                .ptPa("PT PA")
                .station("Station 1")
                .build())
            .amount(AmountDTO.builder()
                .amount(100.0)
                .fee(1.5)
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint("Checkout")
                .paymentMethod("Credit Card")
                .isCart(false)
                .build())
            .build();
    }

    @Override
    public TokenInfoDTO getTokenInfo(String token) {
        log.debug("Request to get token info: {}", token);
        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav("123456789012345678")
                .paEmittente("12345678901")
                .iuv("iuv123")
                .lastEvent(Instant.now())
                .build())
            .isPayedToken(true)
            .payed(PayedDTO.builder()
                .token(token)
                .payedDate(Instant.now())
                .build())
            .actors(ActorsDTO.builder().psp("PSP Test").build())
            .amount(AmountDTO.builder().amount(50.0).fee(0.5).build())
            .paymentInfo(PaymentInfoDTO.builder().touchpoint("Redirect").build())
            .build();
    }

    @Override
    public TransferPaymentDTO getTransfers(String nav, String paEmittente, String token) {
        log.debug("Request to get transfers for: {}, {}, {}", nav, paEmittente, token);
        return TransferPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(nav)
                .paEmittente(paEmittente)
                .build())
            .token(token)
            .transfersCount(1.0)
            .transfers(TransferObjectDTO.builder()
                .idTransfer(1)
                .typeTransfer("sepa")
                .iban("IT00X0123456789012345678901")
                .amount(100.0)
                .paFiscalCode(paEmittente)
                .build())
            .build();
    }

    @Override
    public WorkflowResponseDTO getWorkflows(String nav, String paEmittente) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);
        WorkflowObjectDTO eventPos = WorkflowObjectDTO.builder()
            .insertedtimestamp(Instant.now())
            .tipoevento("POS_EVENT")
            .outcome("OK")
            .eventId("evt1")
            .build();
        WorkflowTokenObjectDTO eventTok = WorkflowTokenObjectDTO.builder()
            .insertedtimestamp(Instant.now())
            .tipoevento("TOKEN_EVENT")
            .outcome("OK")
            .token("abcde12345abcde12345abcde1234512")
            .eventId("evt2")
            .build();
        return WorkflowResponseDTO.builder()
            .count(2.0)
            .eventsPosition(Collections.singletonList(eventPos))
            .eventsToken(Collections.singletonList(eventTok))
            .build();
    }

    @Override
    public ExtraInfoResponseDTO getExtraInfo(String token) {
        log.debug("Request to get extra info for token: {}", token);
        ExtraInfoObjectDTO extra = ExtraInfoObjectDTO.builder()
            .nav("123456789012345678")
            .paEmittente("12345678901")
            .token(token)
            .name("Extra Param")
            .value("Extra Value")
            .tipoevento("EXTRA_EVENT")
            .build();
        return ExtraInfoResponseDTO.builder()
            .count(1)
            .results(Collections.singletonList(extra))
            .build();
    }
}
