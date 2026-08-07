package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchPerimeterFileRepository extends JpaRepository<SearchPerimeterFile, UUID> {

    Optional<SearchPerimeterFile> findTopByInstanceOrderByCreatedAtDesc(SearchInstance instance);
}
