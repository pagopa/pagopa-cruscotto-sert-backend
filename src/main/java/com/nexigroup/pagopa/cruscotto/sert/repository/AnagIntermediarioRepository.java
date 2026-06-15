package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagIntermediarioRepository extends JpaRepository<AnagIntermediario, Short> {
	Optional<AnagIntermediario> findOneByCodice(String codice);
}

