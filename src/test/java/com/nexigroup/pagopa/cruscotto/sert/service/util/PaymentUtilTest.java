package com.nexigroup.pagopa.cruscotto.sert.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.undertow.util.BadRequestException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaymentUtilTest {

    @Test
    void convertsHexTokensOnlyWhenDecodedValueIsHex() {
        assertThat(PaymentUtil.tokenAsHex("61626364")).isEqualTo("abcd");
        assertThat(PaymentUtil.tokenAsHex("zz")).isEqualTo("zz");
        assertThat(PaymentUtil.tokenAsHex("6162636")).isEqualTo("6162636");
        assertThat(PaymentUtil.tokenAsHex(null)).isNull();
        assertThat(PaymentUtil.tokenAsHex("  ")).isEqualTo("  ");
    }

    @Test
    void convertsSupportedNumbersAndDateTypesToCommonValues() {
        Instant instant = Instant.parse("2026-01-02T03:04:05Z");
        LocalDate date = LocalDate.of(2026, 1, 2);

        assertThat(PaymentUtil.toDouble(new BigDecimal("12.5"))).isEqualTo(12.5);
        assertThat(PaymentUtil.toDouble(12)).isEqualTo(12.0);
        assertThat(PaymentUtil.toDouble("2.75")).isEqualTo(2.75);
        assertThat(PaymentUtil.toDouble(null)).isNull();
        assertThat(PaymentUtil.toInstant(instant)).isEqualTo(instant);
        assertThat(PaymentUtil.toInstant(Timestamp.from(instant))).isEqualTo(instant);
        assertThat(PaymentUtil.toInstant(LocalDateTime.of(2026, 1, 2, 3, 4, 5))).isEqualTo(instant);
        assertThat(PaymentUtil.toInstant(Date.valueOf(date))).isEqualTo(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        assertThat(PaymentUtil.toInstant(date)).isEqualTo(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        assertThat(PaymentUtil.toInstant("unsupported")).isNull();
        assertThat(PaymentUtil.toInstantFromDate(date)).isEqualTo(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    @Test
    void parsesAggregatedInfoAndRemapsSortProperties() {
        assertThat(PaymentUtil.parseInfoMatch(" alpha, beta ,,gamma ")).containsExactly("alpha", "beta", "gamma");
        assertThat(PaymentUtil.parseInfoMatch("  ")).isEmpty();
        assertThat(PaymentUtil.parseInfoMatch(null)).isEmpty();

        Pageable pageable = PageRequest.of(2, 15, Sort.by(Sort.Order.desc("inputType")));
        Pageable mapped = PaymentUtil.remapSorting(pageable, List.of(Sort.Order.asc("id")),
            Map.of("inputType", "input_type"), List.of(Sort.Order.desc("created_at")));
        assertThat(mapped.getPageNumber()).isEqualTo(2);
        assertThat(mapped.getPageSize()).isEqualTo(15);
        assertThat(mapped.getSort().getOrderFor("input_type").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(mapped.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(PaymentUtil.remapSorting(null, null, Map.of(), List.of(Sort.Order.desc("created_at"))).getPageSize()).isEqualTo(20);
    }

    @Test
    void rejectsUnmappedAndForbiddenSortFields() throws Exception {
        assertThatThrownBy(() -> PaymentUtil.validatePageable(PageRequest.of(0, 10, Sort.by("invalid")), Map.of("name", "name")))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid sort fields");
        assertThatThrownBy(() -> PaymentUtil.validatePageable(PageRequest.of(0, 10, Sort.by("idTransfer")), PaymentUtil.TRANSFER_SORT_MAPPING))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("idTransfer cannot be passed");
        assertThat(PaymentUtil.validatePageable(PageRequest.of(0, 10), Map.of())).isNull();
    }
}