package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for SERT APIs with Mock Data.
 */
@Service
public class SertServiceImpl implements SertService {

    private final Logger log = LoggerFactory.getLogger(SertServiceImpl.class);

    @Override
    public SearchResultsResponseDTO searchByNav(String nav) {
        log.debug("Request to search by NAV: {}", nav);
        BasicPositionObjectDTO result = BasicPositionObjectDTO.builder()
            .nav(nav)
            .paEmittente("12345678901")
            .build();
        return SearchResultsResponseDTO.builder()
            .results(Collections.singletonList(result))
            .count(1)
            .build();
    }

    @Override
    public SearchResultsResponseDTO searchByIuv(String iuv) {
        log.debug("Request to search by IUV: {}", iuv);
        BasicPositionObjectDTO result = BasicPositionObjectDTO.builder()
            .nav("123456789012345678")
            .paEmittente("12345678901")
            .build();
        return SearchResultsResponseDTO.builder()
            .results(Collections.singletonList(result))
            .count(1)
            .build();
    }

    @Override
    public SearchResultsResponseDTO searchByCart(String idCart) {
        log.debug("Request to search by Cart: {}", idCart);
        BasicPositionObjectDTO result = BasicPositionObjectDTO.builder()
            .nav("123456789012345678")
            .paEmittente("12345678901")
            .build();
        return SearchResultsResponseDTO.builder()
            .results(Collections.singletonList(result))
            .count(1)
            .build();
    }

    @Override
    public SearchResultsResponseDTO searchByToken(String token) {
        log.debug("Request to search by Token: {}", token);
        BasicPositionObjectDTO result = BasicPositionObjectDTO.builder()
            .nav("123456789012345678")
            .paEmittente("12345678901")
            .build();
        return SearchResultsResponseDTO.builder()
            .results(Collections.singletonList(result))
            .count(1)
            .build();
    }

    @Override
    public SearchResultsExtraResponseDTO searchExtra(String searchValue) {
        log.debug("Request to search extra: {}", searchValue);
        PositionPaymentExtraDTO result = PositionPaymentExtraDTO.builder()
            .nav("123456789012345678")
            .paEmittente("12345678901")
            .match(Collections.singletonList("rrn: " + searchValue))
            .build();
        return SearchResultsExtraResponseDTO.builder()
            .results(Collections.singletonList(result))
            .count(1)
            .build();
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
