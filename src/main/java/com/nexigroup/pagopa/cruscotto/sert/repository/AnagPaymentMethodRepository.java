package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnagPaymentMethodRepository extends JpaRepository<AnagPaymentMethod, Short> {

    @Query("SELECT e.codice FROM AnagPaymentMethod e")
    Page<String> findAllCodes(Pageable pageable);

    @Query("SELECT e.codice FROM AnagPaymentMethod e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<String> findAllCodesWithSearch(@Param("search") String search, Pageable pageable);
}