package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;

/**
 * Service Interface for SERT APIs.
 */
public interface SertService {
    UnifiedSearchResponseDTO searchByNav(String nav, String pa);
    UnifiedSearchResponseDTO searchByIuv(String pa, String nav, String iuv);
    UnifiedSearchResponseDTO searchByCart(String pa, String nav, String idCart);
    UnifiedSearchResponseDTO searchByToken( String pa, String nav,String token);
    UnifiedSearchResponseDTO searchExtra(String pa, String nav, String searchValue);
    PositionPaymentDTO getPosition(String nav, String paEmittente);
    TokenInfoDTO getTokenInfo(String token);
    TransferPaymentDTO getTransfers(String nav, String paEmittente, String token);
    WorkflowResponseDTO getWorkflows(String nav, String paEmittente);
    ExtraInfoResponseDTO getExtraInfo(String token);
}
