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
 * Entity representing an execution record (SEARCH_EXECUTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_EXECUTION", schema = "sert_ingestor")
public class SearchExecution {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private SearchInstance instance;

    @Column(name = "STATUS", nullable = false, length = 16)
    private String status;

    @Column(name = "STARTED_AT")
    private Instant startedAt;

    @Column(name = "COMPLETED_AT")
    private Instant completedAt;

    @Column(name = "TOTAL_INPUT_ROWS")
    private Long totalInputRows;

    @Column(name = "PROCESSED_ROWS")
    private Long processedRows;

    @Column(name = "GENERATED_FILES")
    private Integer generatedFiles;

    @Column(name = "ERROR_CODE", length = 128)
    private String errorCode;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;
}
