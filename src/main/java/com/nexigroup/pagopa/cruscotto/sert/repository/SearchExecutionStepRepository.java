package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchExecutionStep;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchExecutionStepRepository extends JpaRepository<SearchExecutionStep, UUID> {

    Page<SearchExecutionStep> findByExecutionId(UUID executionId, Pageable pageable);
}