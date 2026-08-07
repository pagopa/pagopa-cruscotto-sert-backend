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
 * Entity representing the last available result (SEARCH_RESULT)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_RESULT")
public class SearchResult {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private SearchInstance instance;

    @Column(name = "EXECUTION_ID")
    private UUID executionId;

    @Column(name = "ZIP_BLOB_PATH")
    private String zipBlobPath;

    @Column(name = "ROWS_COUNT")
    private Integer rowsCount;

    @Column(name = "GENERATED_AT")
    private Instant generatedAt;
}
