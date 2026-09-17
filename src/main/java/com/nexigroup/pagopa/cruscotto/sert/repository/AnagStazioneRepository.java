package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagStazione;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagStazioneRepository extends JpaRepository<AnagStazione, Short> {
    Optional<AnagStazione> findOneByCodice(String codice);

    @Query("SELECT e FROM AnagStazione e WHERE LOWER(e.codice) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AnagStazione> findAllWithSearch(@Param("search") String search, Pageable pageable);
}


