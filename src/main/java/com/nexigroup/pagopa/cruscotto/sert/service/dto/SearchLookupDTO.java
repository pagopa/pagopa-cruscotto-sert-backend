package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLookupDTO {

    private Long id;
    private String codice;
    private String description;
}