package com.nexigroup.pagopa.cruscotto.sert.domain;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplate;
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
@Table(name = "SEARCH_PERIMETER_FILE", schema = "sert_ingestor")
public class SearchPerimeterFile {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private SearchInstance instance;

    @Column(name = "EXECUTION_ID")
    private UUID executionId;

    @Column(name = "SOURCE", nullable = false, length = 32)
    private String source;

    @Column(name = "TEMPLATE", length = 16)
    private CsvTemplate template;

    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;

    @Column(name = "FILE_PATH", length = 1024)
    private String filePath;

    @Column(name = "ROWS_COUNT")
    private Long rowsCount;

    @Column(name = "VALIDATION_STATUS", nullable = false, length = 16)
    private String validationStatus;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "CONTENT", columnDefinition = "text")
    private String content;
}
