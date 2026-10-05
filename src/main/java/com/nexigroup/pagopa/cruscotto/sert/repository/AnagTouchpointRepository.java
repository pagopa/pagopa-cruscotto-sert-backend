package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagTouchpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnagTouchpointRepository extends JpaRepository<AnagTouchpoint, Short> {

    Optional<AnagTouchpoint> findByCodice(String codice);

    @Query("SELECT e FROM AnagTouchpoint e")
    Page<AnagTouchpoint> findAllPaged(Pageable pageable);

    @Query("SELECT e FROM AnagTouchpoint e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AnagTouchpoint> findAllPagedWithSearch(@Param("search") String search, Pageable pageable);
}