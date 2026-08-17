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
 * Entity representing search filter (SEARCH_FILTER)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_FILTER", schema = "sert_ingestor")
public class SearchFilter {

    /**
     * Primary key is INSTANCE_ID (uuid)
     */
    @Id
    @Column(name = "INSTANCE_ID", nullable = false)
    private UUID instanceId;

    /**
     * Convenience association to SearchInstance. Marked non-insertable/non-updatable
     * because INSTANCE_ID is the PK mapped by instanceId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", insertable = false, updatable = false)
    private SearchInstance instance;

    @Column(name = "FILTER_JSON", columnDefinition = "jsonb", nullable = false)
    private String filterJson;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;
}
