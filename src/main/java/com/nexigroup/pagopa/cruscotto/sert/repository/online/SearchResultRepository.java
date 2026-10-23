package com.nexigroup.pagopa.cruscotto.sert.repository.online;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchResult;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchResultRepository extends JpaRepository<SearchResult, UUID> {
}
