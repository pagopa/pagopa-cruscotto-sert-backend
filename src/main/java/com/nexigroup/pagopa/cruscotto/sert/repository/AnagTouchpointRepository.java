package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagTouchpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnagTouchpointRepository extends JpaRepository<AnagTouchpoint, Short> {

    @Query("SELECT e.codice FROM AnagTouchpoint e")
    Page<String> findAllCodes(Pageable pageable);

    @Query("SELECT e.codice FROM AnagTouchpoint e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<String> findAllCodesWithSearch(@Param("search") String search, Pageable pageable);
}