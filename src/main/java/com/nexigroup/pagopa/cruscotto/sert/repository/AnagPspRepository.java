package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPsp;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagPspRepository extends JpaRepository<AnagPsp, Short> {
    Optional<AnagPsp> findOneByCodice(String codice);

    @Query("SELECT e FROM AnagPsp e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AnagPsp> findAllWithSearch(@Param("search") String search, Pageable pageable);
}

