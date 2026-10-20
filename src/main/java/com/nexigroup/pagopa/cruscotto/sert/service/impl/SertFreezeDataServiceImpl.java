package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.repository.freeze.FreezePositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertFreezeDataService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SertFreezeDataServiceImpl implements SertFreezeDataService {

    private final FreezePositionRepository freezePositionRepository;

    public SertFreezeDataServiceImpl(FreezePositionRepository freezePositionRepository) {
        this.freezePositionRepository = freezePositionRepository;
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchOldPayment(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCart,
        String info,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    ) {
        return freezePositionRepository.search(pa, nav, iuv, token, idCart, info, dateFrom, dateTo, pageable);
    }
}
