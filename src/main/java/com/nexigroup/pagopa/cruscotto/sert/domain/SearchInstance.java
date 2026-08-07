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
@Table(name = "SEARCH_INSTANCE")
public class SearchInstance {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "INPUT_TYPE")
    private String inputType;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ARCHIVED_AT")
    private Instant archivedAt;

    @Column(name = "LAST_EXECUTION_ID")
    private UUID lastExecutionId;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
}
