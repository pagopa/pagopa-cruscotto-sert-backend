package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPaEmittente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagPaEmittenteRepository extends JpaRepository<AnagPaEmittente, Short> {
    Optional<AnagPaEmittente> findOneByCodice(String codice);
}
