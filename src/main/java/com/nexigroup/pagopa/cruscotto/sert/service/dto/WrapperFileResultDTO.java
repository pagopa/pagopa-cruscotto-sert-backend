package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WrapperFileResultDTO {

    String fileName;
    byte[] content;

}
