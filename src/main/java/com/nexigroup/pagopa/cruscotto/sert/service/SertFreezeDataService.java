package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SertFreezeDataService {

    Page<PositionPaymentExtraDTO> searchOldPayment(
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
}
