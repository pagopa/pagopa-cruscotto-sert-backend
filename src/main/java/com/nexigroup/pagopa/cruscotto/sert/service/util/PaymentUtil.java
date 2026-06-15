package com.nexigroup.pagopa.cruscotto.sert.service.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public final class PaymentUtil {

    private PaymentUtil() {
        // Utility class
    }

    public static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    public static String tokenAsHex(Object value) {
        String token = asString(value);
        if (token == null || token.isBlank() || !isHex(token) || token.length() % 2 != 0) {
            return token;
        }

        try {
            String decodedToken = new String(HexFormat.of().parseHex(token), StandardCharsets.UTF_8);
            return isHex(decodedToken) ? decodedToken : token;
        } catch (IllegalArgumentException e) {
            return token;
        }
    }

    private static boolean isHex(String value) {
        return value.chars().allMatch(character ->
            (character >= '0' && character <= '9') ||
            (character >= 'a' && character <= 'f') ||
            (character >= 'A' && character <= 'F')
        );
    }

    public static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    public static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        if (value instanceof Date date) {
            return date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        return null;
    }

    public static Instant toInstantFromDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        return toInstant(value);
    }

    public static List<String> parseInfoMatch(Object aggregatedInfoNames) {
        if (aggregatedInfoNames == null) {
            return Collections.emptyList();
        }
        String value = aggregatedInfoNames.toString().trim();
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .collect(Collectors.toList());
    }
}

