package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagIntermediarioPaRepository extends JpaRepository<AnagIntermediarioPa, Short> {
    Optional<AnagIntermediarioPa> findOneByCodice(String codice);
}
