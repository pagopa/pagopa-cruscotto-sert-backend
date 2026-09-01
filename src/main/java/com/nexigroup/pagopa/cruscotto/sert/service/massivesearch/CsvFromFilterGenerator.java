package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a CSV (NAV:paEmittente) based on the provided SearchBulkFilterDTO.
 * The generator builds a native SQL dynamically adding predicates only for non-null filter fields.
 *
 * Note: for very large result sets consider streaming / pagination to avoid OOM.
 */
@Component
public class CsvFromFilterGenerator {

    private final EntityManager em;

    public CsvFromFilterGenerator(EntityManager em) {
        this.em = em;
    }

    public byte[] generateCsv(SearchBulkFilterDTO filter) {
        if (filter == null) return new byte[0];

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT p.nav, p.pa_emittente FROM position p \n");
        sql.append("JOIN position_tokens pt ON pt.fk_position = p.id \n");

        // join extra info only if needed later (not used now)

        sql.append("WHERE 1=1 \n");

        Map<String, Object> params = new HashMap<>();

        // payment period
        if (filter.getPaymentPeriod() != null) {
            if (filter.getPaymentPeriod().getFrom() != null) {
                sql.append(" AND pt.payment_date >= :paymentFrom ");
                params.put("paymentFrom", Date.valueOf(filter.getPaymentPeriod().getFrom()));
            }
            if (filter.getPaymentPeriod().getTo() != null) {
                sql.append(" AND pt.payment_date <= :paymentTo ");
                params.put("paymentTo", Date.valueOf(filter.getPaymentPeriod().getTo()));
            }
        }

        // paymentStatuses
        if (!CollectionUtils.isEmpty(filter.getPaymentStatuses())) {
            sql.append(" AND pt.outcome IN (:paymentStatuses) ");
            params.put("paymentStatuses", filter.getPaymentStatuses());
        }

        // touchpoints
        if (!CollectionUtils.isEmpty(filter.getTouchpoints())) {
            sql.append(" AND pt.touchpoint IN (:touchpoints) ");
            params.put("touchpoints", filter.getTouchpoints());
        }

        // paymentMethods
        if (!CollectionUtils.isEmpty(filter.getPaymentMethods())) {
            sql.append(" AND pt.payment_method IN (:paymentMethods) ");
            params.put("paymentMethods", filter.getPaymentMethods());
        }

        // amount
        if (filter.getAmount() != null) {
            if (filter.getAmount().getExact() != null) {
                sql.append(" AND pt.amount = :amountExact ");
                params.put("amountExact", filter.getAmount().getExact());
            } else {
                if (filter.getAmount().getMin() != null) {
                    sql.append(" AND pt.amount >= :amountMin ");
                    params.put("amountMin", filter.getAmount().getMin());
                }
                if (filter.getAmount().getMax() != null) {
                    sql.append(" AND pt.amount <= :amountMax ");
                    params.put("amountMax", filter.getAmount().getMax());
                }
            }
        }

        // creditors -> p.pa_emittente
        if (!CollectionUtils.isEmpty(filter.getCreditors())) {
            sql.append(" AND p.pa_emittente IN (:creditors) ");
            params.put("creditors", filter.getCreditors());
        }

        // psps -> pt.psp
        if (!CollectionUtils.isEmpty(filter.getPsps())) {
            sql.append(" AND pt.psp IN (:psps) ");
            params.put("psps", filter.getPsps());
        }

        // technologicalPartners -> pt.intermediario_pa or pt.intermediario_psp
        if (!CollectionUtils.isEmpty(filter.getTechnologicalPartners())) {
            sql.append(" AND (pt.intermediario_pa IN (:techPartners) OR pt.intermediario_psp IN (:techPartners)) ");
            params.put("techPartners", filter.getTechnologicalPartners());
        }

        // channels
        if (!CollectionUtils.isEmpty(filter.getChannels())) {
            sql.append(" AND pt.canale IN (:channels) ");
            params.put("channels", filter.getChannels());
        }

        // stations
        if (!CollectionUtils.isEmpty(filter.getStations())) {
            sql.append(" AND pt.stazione IN (:stations) ");
            params.put("stations", filter.getStations());
        }

        sql.append(" ORDER BY p.nav, p.pa_emittente ");

        Query q = em.createNativeQuery(sql.toString());

        // set params
        for (Map.Entry<String, Object> e : params.entrySet()) {
            Object v = e.getValue();
            if (v instanceof List) {
                // Hibernate supports setting collection parameter on native queries in many dialects;
                // fallback: setParameter with the List will work on JPA providers that support it.
                q.setParameter(e.getKey(), v);
            } else {
                q.setParameter(e.getKey(), v);
            }
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (Object[] r : rows) {
                String nav = r[0] == null ? "" : String.valueOf(r[0]);
                String pa = r[1] == null ? "" : String.valueOf(r[1]);
                String line = nav + ":" + pa + "\n";
                out.write(line.getBytes(StandardCharsets.UTF_8));
            }
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to build CSV from query result", ex);
        }
    }
}
