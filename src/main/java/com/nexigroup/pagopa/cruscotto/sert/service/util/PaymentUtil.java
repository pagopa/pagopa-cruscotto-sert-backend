package com.nexigroup.pagopa.cruscotto.sert.service.util;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.ExtraInfoObjectDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import io.undertow.util.BadRequestException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public final class PaymentUtil {

    private final static  Logger log = LoggerFactory.getLogger(PaymentUtil.class);


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
    public static Pageable remapSorting(Pageable pageable, List<Sort.Order> orderSecondColumn, Map<String, String> sortMapping, List<Sort.Order> orderDefault) {
        List<Sort.Order> orders = new ArrayList<>();

        if (pageable != null && pageable.getSort().isSorted()) {
            pageable.getSort().stream()
                .map(order -> {
                    String prop = order.getProperty();
                    String mapped = sortMapping.getOrDefault(prop, prop);
                    return new Sort.Order(order.getDirection(), mapped);
                })
                .forEach(orders::add);
        }else {
            orders.addAll(orderDefault);
        }

        if (orderSecondColumn != null) {
            orders.addAll(orderSecondColumn);
        }

        Sort mappedSort = Sort.by(orders);

        if (pageable == null) {
            return PageRequest.of(0, 20, mappedSort);
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
    }

    public  static final Map<String, String> TRANSFER_SORT_MAPPING = Map.of(
        "idTransfer", "idTransfer",
        "typeTransfer", "isBollo",
        "iban", "ibanTransfer",
        "amount", "amountTransfer",
        "paFiscalCode", "paTransfer"
    );

    public  static Object transferSortValue(Object[] row, String property) {
        return switch (property) {
            case "idTransfer" -> row[10];
            case "typeTransfer" -> Boolean.TRUE.equals(row[11]) ? "bollo" : "sepa";
            case "isBollo" -> row[11];
            case "iban", "ibanTransfer" -> row[12];
            case "amount", "amountTransfer" -> row[13];
            case "paFiscalCode", "paTransfer" -> row[14];
            case "transferDateEvent", "dateEvent" -> row.length > 15 ? row[15] : null;
            default -> null;
        };
    }

    public  static final Map<String, String> POSITION_TOKEN_SORT_MAPPING = Map.of(
        "tokenDateEvent", "tokenDateEvent",
        "psp", "psp",
        "ptPsp", "ptPsp",
        "amount", "amount",
        "paymentMethod", "paymentMethod",
        "paymentDate","paymentDate",
        "touchpoint", "touchpoint",
        "pt-pa-description", "ptPaDesc",
        "token","tokenHex"

    );

    public  static Object positionSortValue(Object[] row, String property) {
        return switch (property) {
            case "tokenDateEvent" -> toInstantFromDate(row[8]);
            case "paymentDate" -> toInstant(row[9]);
            case "psp" -> asString(row[13]);
            case "ptPsp" -> asString(row[15]);
            case "amount" -> row[11] instanceof Number number ? number.doubleValue() : null;
            case "paymentMethod" -> asString(row[19]);
            case "touchpoint" -> asString(row[18]);
            case "pt-pa-description", "ptPaDesc" -> asString(row[22]);
            case "tokenHex" -> asString(row[7]);
            default -> null;
        };
    }

    public static final Map<String, String> EXTRA_INFO_SORT_MAPPING = Map.of(
        "nav", "nav",
        "pa-emittente", "paEmittente",
        "token", "token",
        "name", "infoName",
        "value", "infoValue",
        "tipoevento", "tipoEvento"
    );

    public static String extraInfoSortValue(ExtraInfoObjectDTO result, String property) {
        return switch (property) {
            case "nav" -> result.getNav();
            case "paEmittente", "pa-emittente" -> result.getPaEmittente();
            case "token" -> result.getToken();
            case "name", "infoName" -> result.getName();
            case "value", "infoValue" -> result.getValue();
            case "tipoevento", "tipoEvento" -> result.getTipoevento();
            default -> null;
        };

    }

    public static final Map<String, String> SEARCH_SORT_MAPPING = Map.of(
        "nav", "nav",
        "pa-emittente", "paEmittente",
        "pa-emittente-desc", "paEmittenteDesc",
        "paEmittente", "paEmittente"
    );

    public static String extraSortValue(PositionPaymentExtraDTO row, String property) {
        return switch (property) {
            case "nav" -> row.getNav();
            case "pa-emittente", "paEmittente" -> row.getPaEmittente();
            case "pa-emittente-desc", "paEmittenteDesc" -> row.getPaEmittenteDsc();
            default -> null;
        };
    }

    public static final Map<String, String> SEARCH_INSTANCE_SORT_MAPPINGS = Map.ofEntries(
        Map.entry("id", "id"),
        Map.entry("name", "name"),
        Map.entry("inputType", "input_type"),
        Map.entry("selectedReports", "selected_reports"),
        Map.entry("status", "status"),
        Map.entry("createdAt", "created_at"),
        Map.entry("updatedAt", "updated_at")
    );

    public static final Map<String, String> WORKFLOW_QUERY_TO_DTO_MAPPING = Map.of(
        "insertedtimestamp", "insertedtimestamp",
        "tipoevento", "nomeevento",
        "sottotipoevento", "tipoevento",
        "outcome", "outcome",
        "faultcode", "faultcode",
        "event-id", "eventid",
        "token", "token",
        "corr-id","id"
    );

    public static Object workflowSortValue(Object[] row, String property) {
        return switch (property) {
            case "insertedtimestamp", "insertedTimestamp" -> row[0];
            case "nomeevento", "tipoEvento" -> row[1];
            case "tipoevento", "sottotipoEvento" -> row[2];
            case "outcome" -> row[3];
            case "eventId", "event-id" -> row[4];
            case "faultcode", "faultCode" -> row[5];
            case "token" -> row[6];
            case "corr-id","id" -> row[8];
            default -> null;

        };
    }

    public  static ResponseEntity<String> validatePageable(Pageable pageable, Map<String, String> mapAttribute) throws BadRequestException {
        if (pageable.getSort().isSorted()) {
            Set<String> invalid = pageable.getSort().stream()
                .map(Sort.Order::getProperty)
                .filter(p -> !mapAttribute.containsKey(p))
                .collect(Collectors.toSet());
            if (!invalid.isEmpty()) {
                String errorMessage = String.format("Invalid sort fields for transfers: %s. Allowed fields are: %s. Mapping to query properties: %s",
                    invalid, mapAttribute.keySet(), mapAttribute);
                log.error(errorMessage);
                throw new BadRequestException(errorMessage);
            }

            // Check that idTransfer is NOT passed by frontend
            boolean hasIdTransfer = pageable.getSort().stream()
                .anyMatch(order -> "idTransfer".equals(order.getProperty()));
            if (hasIdTransfer) {
                String errorMessage = "idTransfer cannot be passed in sort. It is automatically prepended as first sort in DESC order.";
                log.error(errorMessage);
                throw new BadRequestException(errorMessage);
            }
        }
        return null;
    }

}

