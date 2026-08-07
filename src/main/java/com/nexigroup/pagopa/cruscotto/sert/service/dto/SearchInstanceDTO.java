package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchInstance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchInstanceDTO implements Serializable {
    private UUID id;
    private String name;
    private String inputType; // FILTER | CSV
    private String status; // DRAFT, READY, RUNNING, EXECUTED, FAILED, ARCHIVED
    private Instant createdAt;
    private Instant updatedAt;
}
