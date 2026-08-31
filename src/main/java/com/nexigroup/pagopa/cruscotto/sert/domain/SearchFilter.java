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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // ------------------ helper for JSON (de)serialization ------------------
    private static final Logger log = LoggerFactory.getLogger(SearchFilter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Returns the deserialized SearchBulkFilterDTO represented by filterJson, or null
     * if filterJson is null or cannot be deserialized.
     */
    public SearchBulkFilterDTO getFilterObject() {
        if (this.filterJson == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(this.filterJson, SearchBulkFilterDTO.class);
        } catch (JsonProcessingException e) {
            // Log and return null to avoid 500 on (de)serialization errors; adjust if you prefer exceptions
            log.error("Errore deserializzazione filterJson per SearchFilter instanceId={}", this.instanceId, e);
            return null;
        }
    }

    /**
     * Serializes the provided SearchBulkFilterDTO into filterJson. If filter is null,
     * filterJson is set to null. On serialization error, logs and sets filterJson to null.
     */
    public void setFilterObject(SearchBulkFilterDTO filter) {
        if (filter == null) {
            this.filterJson = null;
            return;
        }
        try {
            this.filterJson = OBJECT_MAPPER.writeValueAsString(filter);
        } catch (JsonProcessingException e) {
            log.error("Errore serializzazione SearchBulkFilterDTO per SearchFilter instanceId={}", this.instanceId, e);
            this.filterJson = null;
        }
    }
}
