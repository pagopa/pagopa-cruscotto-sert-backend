package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;

/**
 * Service Interface for SERT APIs.
 */
public interface SertService {
    UnifiedSearchResponseDTO searchByNav(String nav, String pa, int offset, int limit);
    UnifiedSearchResponseDTO searchByIuv(String pa, String nav, String iuv, int offset, int limit);
    UnifiedSearchResponseDTO searchByCart(String pa, String nav, String idCart, int offset, int limit);
    UnifiedSearchResponseDTO searchByToken( String pa, String nav,String token, int offset, int limit);
    UnifiedSearchResponseDTO searchExtra(String pa, String nav, String searchValue, int offset, int limit);
    PositionPaymentDTO getPosition(String nav, String paEmittente);
    TokenInfoDTO getTokenInfo(String token);
    TransferPaymentDTO getTransfers(String nav, String paEmittente, String token);
    WorkflowResponseDTO getWorkflows(String nav, String paEmittente);
    ExtraInfoResponseDTO getExtraInfo(String token);
}
