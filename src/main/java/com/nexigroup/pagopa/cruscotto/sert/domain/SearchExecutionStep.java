package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a step of a Search Execution (SEARCH_EXECUTION_STEP)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_EXECUTION_STEP", schema = "sert_ingestor")
public class SearchExecutionStep {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "EXECUTION_ID", nullable = false)
    private UUID executionId;

    @Column(name = "INSTANCE_ID", nullable = false)
    private UUID instanceId;

    @Column(name = "PHASE", nullable = false, length = 32)
    private String phase;

    @Column(name = "ATTEMPT_NO", nullable = false)
    private Integer attemptNo;

    @Column(name = "STATUS", nullable = false, length = 16)
    private String status;

    @Column(name = "WINDOW_FROM")
    private LocalDateTime windowFrom;

    @Column(name = "WINDOW_TO")
    private LocalDateTime windowTo;

    @Column(name = "ROWS_PROCESSED")
    private Long rowsProcessed;

    @Column(name = "STARTED_AT", nullable = false)
    private Instant startedAt;

    @Column(name = "ENDED_AT")
    private Instant endedAt;

    @Column(name = "DURATION_MS")
    private Long durationMs;

    @Column(name = "ERROR_CODE", length = 128)
    private String errorCode;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
}
