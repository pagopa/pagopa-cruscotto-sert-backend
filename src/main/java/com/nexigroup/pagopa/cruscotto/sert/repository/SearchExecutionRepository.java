package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchExecutionRepository extends JpaRepository<SearchExecution, UUID> {
	Page<SearchExecution> findByInstance_Id(UUID instanceId, Pageable pageable);
}
