package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchFilter;
import java.util.UUID;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchFilterRepository extends JpaRepository<SearchFilter, UUID> {
    // CRUD is enough; findById inherited
}
