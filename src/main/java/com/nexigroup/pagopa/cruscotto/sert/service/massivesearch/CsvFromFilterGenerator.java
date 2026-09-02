// src/main/java/com/nexigroup/pagopa/cruscotto/sert/service/massivesearch/CsvFromFilterGenerator.java
package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch;

import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.PerimeterPaymentStatus;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvFromFilterGenerator {

    private static final Logger log = LoggerFactory.getLogger(CsvFromFilterGenerator.class);

    private final SearchInstanceRepository searchInstanceRepository;

    public CsvFromFilterGenerator(SearchInstanceRepository searchInstanceRepository) {
        this.searchInstanceRepository = searchInstanceRepository;
    }

    public byte[] generateCsv(SearchBulkFilterDTO filter) {
        if (filter == null) return new byte[0];

        try {
            Date paymentFrom = (filter.getPaymentPeriod() != null && filter.getPaymentPeriod().getFrom() != null)
                ? Date.valueOf(filter.getPaymentPeriod().getFrom()) : null;
            Date paymentTo = (filter.getPaymentPeriod() != null && filter.getPaymentPeriod().getTo() != null)
                ? Date.valueOf(filter.getPaymentPeriod().getTo()) : null;

            BigDecimal amountExact = null;
            BigDecimal amountMin = null;
            BigDecimal amountMax = null;
            if (filter.getAmount() != null) {
                amountExact = filter.getAmount().getExact();
                amountMin = filter.getAmount().getMin();
                amountMax = filter.getAmount().getMax();
            }

            // Convert paymentStatuses (enum) to strings; detect NO_OUTCOME
            boolean includeNoOutcome = false;
            List<PerimeterPaymentStatus> enumStatuses = filter.getPaymentStatuses();
            List<String> statusStrings = null;
            if (!CollectionUtils.isEmpty(enumStatuses)) {
                statusStrings = new ArrayList<>();
                for (PerimeterPaymentStatus s : enumStatuses) {
                    if (s == null) continue;
                    if (s == PerimeterPaymentStatus.NO_OUTCOME) {
                        includeNoOutcome = true;
                    } else {
                        // OK/KO map to same text stored in DB
                        statusStrings.add(s.name());
                    }
                }
                if (statusStrings.isEmpty()) statusStrings = null; // keep null if only NO_OUTCOME was requested
            }

            List<String> touchpoints = filter.getTouchpoints();
            List<String> paymentMethods = filter.getPaymentMethods();
            List<String> creditors = filter.getCreditors();
            List<Integer> psps = filter.getPsps();
            List<Integer> techPartners = filter.getTechnologicalPartners();
            List<Integer> channels = filter.getChannels();
            List<Integer> stations = filter.getStations();

            List<Object[]> rows = searchInstanceRepository.findNavPaByFilter(
                paymentFrom,
                paymentTo,
                statusStrings,
                includeNoOutcome ? Boolean.TRUE : Boolean.FALSE,
                CollectionUtils.isEmpty(touchpoints) ? null : touchpoints,
                CollectionUtils.isEmpty(paymentMethods) ? null : paymentMethods,
                amountExact,
                amountMin,
                amountMax,
                CollectionUtils.isEmpty(creditors) ? null : creditors,
                CollectionUtils.isEmpty(psps) ? null : psps,
                CollectionUtils.isEmpty(techPartners) ? null : techPartners,
                CollectionUtils.isEmpty(channels) ? null : channels,
                CollectionUtils.isEmpty(stations) ? null : stations
            );

            if (rows == null || rows.isEmpty()) return new byte[0];

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                for (Object[] r : rows) {
                    String nav = r.length > 0 && r[0] != null ? String.valueOf(r[0]) : "";
                    String pa = r.length > 1 && r[1] != null ? String.valueOf(r[1]) : "";
                    out.write((nav + ":" + pa + "\n").getBytes(StandardCharsets.UTF_8));
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error generating perimeter CSV from filter: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
}
