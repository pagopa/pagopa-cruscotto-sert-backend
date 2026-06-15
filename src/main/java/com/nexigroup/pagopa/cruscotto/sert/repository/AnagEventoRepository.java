package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagEvento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnagEventoRepository extends JpaRepository<AnagEvento, Short> {
    Optional<AnagEvento> findOneByNomeEventoAndTipoEvento(String nomeEvento, String tipoEvento);
}

