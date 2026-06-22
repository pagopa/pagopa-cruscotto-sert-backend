package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for SERT APIs.
 */
public interface SertService {
    Page<PositionPaymentExtraDTO> searchByNav(String nav, String pa, Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByIuv(String pa, String nav, String iuv,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByCart(String pa, String nav, String idCart,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByToken( String pa, String nav,String token,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchExtra(String pa, String nav, String searchValue,Pageable pageable);
    PositionPaymentDTO getPosition(String nav, String paEmittente);
    TokenInfoDTO getTokenInfo(String token);
    TransferPaymentDTO getTransfers(String nav, String paEmittente, String token, Pageable pageable);
    WorkflowResponseDTO getWorkflows(String nav, String paEmittente, Pageable pageable);
    ExtraInfoResponseDTO getExtraInfo(String token, Pageable remappedPageable);
}
