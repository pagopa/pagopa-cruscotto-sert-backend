package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TokenInfoDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TransferPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WorkflowResponseDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.ExtraInfoResponseDTO;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SertFreezeDataService {

    Page<PositionPaymentExtraDTO> searchFromHistory(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCart,
        String info,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    );

    Page<PositionPaymentDTO> getPositionFromHistory(String nav, String paEmittente, Pageable pageable);

    TokenInfoDTO getTokenInfoFromHistory(String token);

    Page<TransferPaymentDTO> getTransfersFromHistory(String nav, String paEmittente, String token, Pageable pageable);

    Page<WorkflowResponseDTO> getWorkflowsFromHistory(String nav, String paEmittente, Pageable pageable);

    Page<ExtraInfoResponseDTO> getExtraInfoFromHistory(String token, Pageable pageable);
}
