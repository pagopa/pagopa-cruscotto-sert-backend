package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.SelectedReports;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.SearchInstanceStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
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
    private PerimeterSearchType inputType; // FILTER | CSV
    private SelectedReports selectedReports;
    private SearchInstanceStatus status; // DRAFT, READY, RUNNING, EXECUTED, FAILED, ARCHIVED
    private Instant createdAt;
    private Instant updatedAt;
    private SearchBulkFilterDTO perimeterFilter;
}
