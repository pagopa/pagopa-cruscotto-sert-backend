package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.SearchInstanceStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating or updating a SearchInstance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchInstanceWriteDTO implements Serializable {
    private UUID id;
    private String name;
    private PerimeterSearchType inputType;
    @Pattern(
        regexp = "^(?!.*\\b(POSITION|TOKEN|TRANSFER)\\b,.*\\b\\1\\b)(POSITION|TOKEN|TRANSFER)(,(POSITION|TOKEN|TRANSFER)){0,2}$",
        message = "selectedReports must contain unique values among POSITION, TOKEN and TRANSFER"
    )
    private String selectedReports;
    private SearchInstanceStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private SearchBulkFilterDTO perimeterFilter;
}