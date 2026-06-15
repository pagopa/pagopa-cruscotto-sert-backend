package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPsp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagPspRepository extends JpaRepository<AnagPsp, Short> {
    Optional<AnagPsp> findOneByCodice(String codice);
}
