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

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query(value = """
        SELECT
            instance.id,
            instance.name,
            instance.input_type,
            instance.status,
            instance.created_at,
            instance.updated_at,
            instance.selected_reports,
            NULLIF(TRIM(CONCAT_WS(' ', creator.nm_last_name, creator.nm_first_name)), '') AS created_by_name,
            NULLIF(TRIM(CONCAT_WS(' ', updater.nm_last_name, updater.nm_first_name)), '') AS updated_by_name
        FROM sert_ingestor.search_instance instance
        LEFT JOIN sert_ingestor.auth_user creator ON creator.co_id = instance.created_by
        LEFT JOIN sert_ingestor.auth_user updater ON updater.co_id = instance.updated_by
        WHERE
            (
                CAST(:instanceId AS UUID) IS NULL
                OR instance.id = CAST(:instanceId AS UUID)
            )
            AND
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
        LEFT JOIN sert_ingestor.auth_user creator ON creator.co_id = instance.created_by
        LEFT JOIN sert_ingestor.auth_user updater ON updater.co_id = instance.updated_by
        WHERE
            (
                CAST(:instanceId AS UUID) IS NULL
                OR instance.id = CAST(:instanceId AS UUID)
            )
            AND
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
    Page<Object[]> findBySearchAndCreatedAtBetween(
        @Param("instanceId") UUID instanceId,
        @Param("name") String name,
        @Param("status") String status,
        @Param("createdFrom") Instant createdFrom,
        @Param("createdTo") Instant createdTo,
        Pageable pageable
    );
}
