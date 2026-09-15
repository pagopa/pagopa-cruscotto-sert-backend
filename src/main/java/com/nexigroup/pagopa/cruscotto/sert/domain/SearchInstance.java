package com.nexigroup.pagopa.cruscotto.sert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Search Instance (SEARCH_INSTANCE)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_INSTANCE", schema = "sert_ingestor")
public class SearchInstance {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "INPUT_TYPE", nullable = false, length = 16)
    private String inputType;

    @Column(name = "STATUS", nullable = false, length = 16)
    private String status;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

    @Column(name = "ARCHIVED_AT")
    private Instant archivedAt;

    @Column(name = "LAST_EXECUTION_ID")
    private UUID lastExecutionId;

    @Column(name = "SELECTED_REPORTS", length = 64)
    private String selectedReports;
}
