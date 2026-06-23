package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ExtraInfoResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtraInfoResponseDTO implements Serializable {
    private Long count;
    private List<ExtraInfoObjectDTO> results;
}
