package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPaEmittente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagPaEmittenteRepository extends JpaRepository<AnagPaEmittente, Short> {
    Optional<AnagPaEmittente> findOneByCodice(String codice);

    @Query("SELECT e FROM AnagPaEmittente e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AnagPaEmittente> findAllWithSearch(@Param("search") String search, Pageable pageable);
}

