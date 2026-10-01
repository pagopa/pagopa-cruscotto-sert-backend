package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.PerimeterPaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchInstancePerimeterFilterDTO {

    private PaymentPeriod paymentPeriod;
    private List<PerimeterPaymentStatus> paymentStatuses;
    private List<SearchLookupDTO> touchpoints;
    private List<SearchLookupDTO> paymentMethods;
    private AmountFilter amount;
    private List<SearchLookupDTO> creditors;
    private List<SearchLookupDTO> psps;
    private List<SearchLookupDTO> technologicalPartnersPa;
    private List<SearchLookupDTO> technologicalPartnersPsp;

    private List<SearchLookupDTO> channels;
    private List<SearchLookupDTO> stations;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentPeriod {
        private LocalDateTime from;
        private LocalDateTime to;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmountFilter {
        private BigDecimal exact;
        private BigDecimal min;
        private BigDecimal max;
    }
}
