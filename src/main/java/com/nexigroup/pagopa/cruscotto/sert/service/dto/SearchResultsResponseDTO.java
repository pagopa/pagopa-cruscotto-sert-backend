package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SearchResultsResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultsResponseDTO implements Serializable {
    private List<BasicPositionObjectDTO> results;
    private Integer count;
}
