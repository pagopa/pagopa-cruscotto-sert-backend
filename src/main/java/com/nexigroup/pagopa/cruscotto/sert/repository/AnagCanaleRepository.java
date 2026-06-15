package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagCanale;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagCanaleRepository extends JpaRepository<AnagCanale, Short> {
    Optional<AnagCanale> findOneByCodice(String codice);
}

