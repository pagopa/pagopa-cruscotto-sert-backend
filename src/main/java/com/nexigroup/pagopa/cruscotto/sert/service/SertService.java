package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service Interface for SERT APIs.
 */
public interface SertService {
    Page<PositionPaymentExtraDTO> searchByNav(String nav, String pa,LocalDate dateFrom ,LocalDate dateTo,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByIuv(String pa, String nav, String iuv,LocalDate dateFrom ,LocalDate dateTo,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByCart(String pa, String nav, String idCart,LocalDate dateFrom ,LocalDate dateTo,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchByToken( String pa, String nav,String token,LocalDate dateFrom ,LocalDate dateTo,Pageable pageable);
    Page<PositionPaymentExtraDTO> searchExtra(String pa, String nav, String searchValue,LocalDate dateFrom ,LocalDate dateTo,Pageable pageable);
    Page<PositionPaymentDTO> getPosition(String nav, String paEmittente, Pageable pageable);
    TokenInfoDTO getTokenInfo(String token);
    Page<TransferPaymentDTO> getTransfers(String nav, String paEmittente, String token, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    Page<WorkflowResponseDTO> getWorkflows(String nav, String paEmittente, Pageable pageable);
    Page<ExtraInfoResponseDTO> getExtraInfo(String token, Pageable pageable);
}
