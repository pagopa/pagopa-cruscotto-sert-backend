package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrapperInstanceCsv {
    SearchInstanceDTO searchInstanceDTO;
    CsvValidationResult result;
}
