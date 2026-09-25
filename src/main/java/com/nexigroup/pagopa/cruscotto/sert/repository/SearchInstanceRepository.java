package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SearchInstanceRepository extends JpaRepository<SearchInstance, UUID> {

    @Query(value = """
        SELECT *
        FROM sert_ingestor.search_instance instance
        WHERE
            (
                CAST(:name AS TEXT) IS NULL
                OR LOWER(instance.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%'))
            )
            AND
            (
                CAST(:status AS TEXT) IS NULL
                OR LOWER(instance.status) LIKE LOWER(CONCAT('%', CAST(:status AS TEXT), '%'))
            )
            AND
            (
                CAST(:createdFrom AS TIMESTAMP WITH TIME ZONE) IS NULL
                OR instance.created_at >= CAST(:createdFrom AS TIMESTAMP WITH TIME ZONE)
            )
            AND
            (
                CAST(:createdTo AS TIMESTAMP WITH TIME ZONE) IS NULL
                OR instance.created_at < CAST(:createdTo AS TIMESTAMP WITH TIME ZONE)
            )
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM sert_ingestor.search_instance instance
        WHERE
            (
                CAST(:name AS TEXT) IS NULL
                OR LOWER(instance.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%'))
            )
            AND
            (
                CAST(:status AS TEXT) IS NULL
                OR LOWER(instance.status) LIKE LOWER(CONCAT('%', CAST(:status AS TEXT), '%'))
            )
            AND
            (
                CAST(:createdFrom AS TIMESTAMP WITH TIME ZONE) IS NULL
                OR instance.created_at >= CAST(:createdFrom AS TIMESTAMP WITH TIME ZONE)
            )
            AND
            (
                CAST(:createdTo AS TIMESTAMP WITH TIME ZONE) IS NULL
                OR instance.created_at < CAST(:createdTo AS TIMESTAMP WITH TIME ZONE)
            )
        """,
        nativeQuery = true)
    Page<SearchInstance> findBySearchAndCreatedAtBetween(
        @Param("name") String name,
        @Param("status") String status,
        @Param("createdFrom") Instant createdFrom,
        @Param("createdTo") Instant createdTo,
        Pageable pageable
    );
}

