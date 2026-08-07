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
 * Entity representing perimeter CSV metadata (SEARCH_PERIMETER_FILE)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SEARCH_PERIMETER_FILE")
public class SearchPerimeterFile {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private SearchInstance instance;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "TEMPLATE")
    private String template;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "BLOB_PATH")
    private String blobPath;

    @Column(name = "ROWS_COUNT")
    private Integer rowsCount;

    @Column(name = "VALIDATION_STATUS")
    private String validationStatus;

    @Column(name = "CREATED_AT")
    private Instant createdAt;
}
