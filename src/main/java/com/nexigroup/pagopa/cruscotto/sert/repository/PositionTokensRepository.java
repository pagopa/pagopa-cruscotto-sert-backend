package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.PositionTokens;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionTokensRepository extends JpaRepository<PositionTokens, Integer> {

    @Query("SELECT DISTINCT p.touchpoint FROM PositionTokens p WHERE p.touchpoint IS NOT NULL")
    Page<String> findDistinctTouchpoints(Pageable pageable);

    @Query("SELECT DISTINCT p.paymentMethod FROM PositionTokens p WHERE p.paymentMethod IS NOT NULL")
    Page<String> findDistinctPaymentMethods(Pageable pageable);
}
