package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SearchInstanceRepository extends JpaRepository<SearchInstance, UUID> {

    @Query(value =
        "SELECT DISTINCT p.nav, p.pa_emittente FROM position p " +
            "JOIN position_tokens pt ON pt.fk_position = p.id " +
            "WHERE (:paymentFrom IS NULL OR pt.payment_date >= :paymentFrom) " +
            "AND (:paymentTo IS NULL OR pt.payment_date <= :paymentTo) " +
            "AND ( " +
            "    (:paymentStatuses IS NULL) OR " +
            "    (COALESCE(:includeNoOutcome, false) = true AND (pt.outcome IS NULL OR trim(pt.outcome) = '')) OR " +
            "    ( :paymentStatuses IS NOT NULL AND pt.outcome IN (:paymentStatuses) ) " +
            ") " +
            "AND (:touchpoints IS NULL OR pt.touchpoint IN (:touchpoints)) " +
            "AND (:paymentMethods IS NULL OR pt.payment_method IN (:paymentMethods)) " +
            "AND (:amountExact IS NULL OR pt.amount = :amountExact) " +
            "AND (:amountMin IS NULL OR pt.amount >= :amountMin) " +
            "AND (:amountMax IS NULL OR pt.amount <= :amountMax) " +
            "AND (:creditors IS NULL OR p.pa_emittente IN (:creditors)) " +
            "AND (:psps IS NULL OR pt.psp IN (:psps)) " +
            "AND (:techPartners IS NULL OR pt.intermediario_pa IN (:techPartners) OR pt.intermediario_psp IN (:techPartners)) " +
            "AND (:channels IS NULL OR pt.canale IN (:channels)) " +
            "AND (:stations IS NULL OR pt.stazione IN (:stations)) " +
            "ORDER BY p.nav, p.pa_emittente",
        nativeQuery = true)
    List<Object[]> findNavPaByFilter(
        @Param("paymentFrom") Date paymentFrom,
        @Param("paymentTo") Date paymentTo,
        @Param("paymentStatuses") List<String> paymentStatuses,
        @Param("includeNoOutcome") Boolean includeNoOutcome,
        @Param("touchpoints") List<String> touchpoints,
        @Param("paymentMethods") List<String> paymentMethods,
        @Param("amountExact") BigDecimal amountExact,
        @Param("amountMin") BigDecimal amountMin,
        @Param("amountMax") BigDecimal amountMax,
        @Param("creditors") List<String> creditors,
        @Param("psps") List<Integer> psps,
        @Param("techPartners") List<Integer> techPartners,
        @Param("channels") List<Integer> channels,
        @Param("stations") List<Integer> stations
    );
}
