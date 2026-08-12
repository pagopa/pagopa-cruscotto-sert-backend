package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity mapping to sert_ingestor.SEARCH_RESULT (schema aligned with provided DDL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_RESULT", schema = "sert_ingestor")
public class SearchResult {

    /**
     * Primary key in the DB is INSTANCE_ID (uuid).
     * Kept as UUID field named id to reduce the number of downstream changes;
     * column in DB is INSTANCE_ID.
     */
    @Id
    @Column(name = "INSTANCE_ID", nullable = false)
    private UUID id;

    /**
     * Optional convenience association to the SearchInstance entity.
     * Marked insertable=false, updatable=false because INSTANCE_ID is the actual column (mapped by 'id' above).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", insertable = false, updatable = false)
    private SearchInstance instance;

    /**
     * FK to search_execution(id) in DB. Kept as UUID for minimal change.
     * If you prefer a full entity relation, replace this UUID with:
     *   @ManyToOne ... private SearchExecution execution;
     * or add a second relation (insertable=false, updatable=false).
     */
    @Column(name = "EXECUTION_ID", nullable = false)
    private UUID executionId;

    @Column(name = "ZIP_FILE_NAME", nullable = false, length = 255)
    private String zipFileName;

    @Column(name = "ZIP_FILE_PATH", nullable = false, length = 1024)
    private String zipFilePath;

    @Column(name = "ZIP_SIZE_BYTES")
    private Long zipSizeBytes;

    @Column(name = "POSITION_ROWS")
    private Long positionRows;

    @Column(name = "ATTEMPT_ROWS")
    private Long attemptRows;

    @Column(name = "TRANSFER_ROWS")
    private Long transferRows;

    @Column(name = "GENERATED_AT", nullable = false)
    private Instant generatedAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;
}
