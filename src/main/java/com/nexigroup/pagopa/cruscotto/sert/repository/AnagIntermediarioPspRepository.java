package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPsp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagIntermediarioPspRepository extends JpaRepository<AnagIntermediarioPsp, Short> {
    Optional<AnagIntermediarioPsp> findOneByCodice(String codice);
}
