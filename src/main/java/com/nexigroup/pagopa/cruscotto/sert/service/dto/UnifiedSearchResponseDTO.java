package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified DTO for search results, allowing both basic and extra results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchResponseDTO implements Serializable {
    private List<PositionPaymentExtraDTO> results;
    private Integer count;
    private Long totalElements;
    private Integer totalPages;
}
