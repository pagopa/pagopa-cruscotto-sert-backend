package com.nexigroup.pagopa.cruscotto.sert.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String TRANSACTION_ID_MDC_KEY = "ctx_transaction_id";
    public static final String CORRELATION_ID_MDC_KEY = "correlation_id";
    public static final String TRACE_ID_MDC_KEY = "trace.id";

    private static final Pattern TRACEPARENT_PATTERN = Pattern.compile(
        "^(?!ff)[0-9a-fA-F]{2}-([0-9a-fA-F]{32})-([0-9a-fA-F]{16})-[0-9a-fA-F]{2}$"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        String correlationId = parseUuid(request.getHeader(CORRELATION_ID_HEADER)).orElseGet(() -> UUID.randomUUID().toString());
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        transactionIdFrom(request).ifPresent(transactionId -> MDC.put(TRANSACTION_ID_MDC_KEY, transactionId));

        if (MDC.get(TRACE_ID_MDC_KEY) == null) {
            traceIdFromTraceparent(request.getHeader("traceparent")).ifPresent(traceId -> MDC.put(TRACE_ID_MDC_KEY, traceId));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (previousContext == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousContext);
            }
        }
    }

    private Optional<String> transactionIdFrom(HttpServletRequest request) {
        return parseUuid(request.getHeader(TRANSACTION_ID_HEADER)).or(() -> parseUuid(request.getHeader(TRANSACTION_ID_MDC_KEY)));
    }

    private Optional<String> parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }

        try {
            UUID uuid = UUID.fromString(value.trim());
            String normalized = uuid.toString();
            return normalized.equalsIgnoreCase(value.trim()) ? Optional.of(normalized) : Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> traceIdFromTraceparent(String traceparent) {
        if (!StringUtils.hasText(traceparent)) {
            return Optional.empty();
        }

        Matcher matcher = TRACEPARENT_PATTERN.matcher(traceparent.trim());
        if (!matcher.matches() || isAllZeros(matcher.group(1)) || isAllZeros(matcher.group(2))) {
            return java.util.Optional.empty();
        }
        return Optional.of(matcher.group(1).toLowerCase());
    }

    private boolean isAllZeros(String value) {
        return value.chars().allMatch(character -> character == '0');
    }
}