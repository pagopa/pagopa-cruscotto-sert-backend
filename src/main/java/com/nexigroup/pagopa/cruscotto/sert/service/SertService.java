package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;

/**
 * Service Interface for SERT APIs.
 */
public interface SertService {
    SearchResultsResponseDTO searchByNav(String nav);
    SearchResultsResponseDTO searchByIuv(String iuv);
    SearchResultsResponseDTO searchByCart(String idCart);
    SearchResultsResponseDTO searchByToken(String token);
    SearchResultsExtraResponseDTO searchExtra(String searchValue);
    PositionPaymentDTO getPosition(String nav, String paEmittente);
    TokenInfoDTO getTokenInfo(String token);
    TransferPaymentDTO getTransfers(String nav, String paEmittente, String token);
    WorkflowResponseDTO getWorkflows(String nav, String paEmittente);
    ExtraInfoResponseDTO getExtraInfo(String token);
}
