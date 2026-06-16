package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagStazione;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagStazioneRepository extends JpaRepository<AnagStazione, Short> {
    Optional<AnagStazione> findOneByCodice(String codice);
}

