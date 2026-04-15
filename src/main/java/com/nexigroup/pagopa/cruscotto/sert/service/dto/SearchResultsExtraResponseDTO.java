package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchResultsExtraResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultsExtraResponseDTO implements Serializable {
    private List<PositionPaymentExtraDTO> results;
    private Integer count;
}
