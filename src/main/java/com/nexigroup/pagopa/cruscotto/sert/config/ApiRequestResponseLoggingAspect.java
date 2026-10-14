package com.nexigroup.pagopa.cruscotto.sert.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@Aspect
@Component
public class ApiRequestResponseLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ApiRequestResponseLoggingAspect.class);
    private static final String REDACTED = "[REDACTED]";
    private static final int MAX_COLLECTION_ITEMS = 100;
    private static final int MAX_STRING_LENGTH = 2_000;
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
        "^(pa|info|value)$|.*(password|token|secret|authorization|cookie|api.?key|access.?key|reset.?key|activation.?key|email|login|first.?name|last.?name|fiscal.?code|codice.?fiscale|paemittente|iban|iuv|nav|cart|carrello|phone|address).*",
        Pattern.CASE_INSENSITIVE
    );

    private final ObjectMapper objectMapper;

    public ApiRequestResponseLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around(
        "execution(public * com.nexigroup.pagopa.cruscotto.sert.web.rest.auth..*Resource.*(..)) || " +
        "execution(public * com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch..*Resource.*(..))"
    )
    public Object logApiRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            Object requestStartNanos = requestAttributes.getRequest().getAttribute(RequestLoggingFilter.REQUEST_START_NANOS_ATTRIBUTE);
            if (requestStartNanos instanceof Long receivedAtNanos) {
                startNanos = receivedAtNanos;
            }
        }
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String action = actionName(method);
        String correlationId = MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY);
        int status = 500;
        Object responseBody = null;
        Throwable failure = null;

        MDC.put("event_action", action);
        MDC.put("event_outcome", "started");

        try {
            Object result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                status = responseEntity.getStatusCode().value();
                responseBody = responseEntity.getBody();
            } else {
                status = responseStatus();
                responseBody = result;
            }
            return result;
        } catch (Throwable throwable) {
            failure = throwable;
            status = failureStatus(throwable);
            throw throwable;
        } finally {
            try {
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                MDC.put("event_action", action);
                MDC.put("event_outcome", failure == null && status < 400 ? "success" : "failure");
                MDC.put("http.response.status_code", Integer.toString(status));
                MDC.put("ctx_details", toJson(Map.of("total_time_executon", formatElapsedTime(elapsedMillis))));
                MDC.put("response", toJson(responseFields(responseBody, failure)));
                LOG.info("API request completed: {} correlationId={}", action, correlationId);
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        }
    }

    static String formatElapsedTime(long elapsedMillis) {
        long minutes = elapsedMillis / 60_000;
        long seconds = (elapsedMillis % 60_000) / 1_000;
        long milliseconds = elapsedMillis % 1_000;

        if (minutes == 0) {
            return seconds + "." + String.format(Locale.ROOT, "%03d", milliseconds);
        }
        return minutes + ":" + String.format(Locale.ROOT, "%02d,%03d", seconds, milliseconds);
    }

    private Map<String, Object> responseFields(Object body, Throwable failure) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (failure != null) {
            response.put("error.type", failure.getClass().getName());
        } else if (body instanceof byte[] bytes) {
            response.put("binaryPayload", true);
            response.put("sizeBytes", bytes.length);
        } else {
            response.put("body", body);
        }
        return response;
    }

    private String actionName(Method method) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        Object routePattern = attributes.getRequest().getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = routePattern instanceof String pattern ? pattern : method.getDeclaringClass().getSimpleName() + "." + method.getName();
        return attributes.getRequest().getMethod() + " " + route;
    }

    private int responseStatus() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? 500 : attributes.getResponse() == null ? 200 : attributes.getResponse().getStatus();
    }

    private int failureStatus(Throwable throwable) {
        if (throwable instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode().value();
        }
        return responseStatus();
    }

    private String toJson(Object value) {
        try {
            JsonNode json = objectMapper.valueToTree(value);
            return objectMapper.writeValueAsString(sanitize(json, null));
        } catch (IllegalArgumentException | java.io.IOException exception) {
            return "{\"serializationError\":true}";
        }
    }

    private JsonNode sanitize(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        if (fieldName != null && SENSITIVE_FIELD.matcher(fieldName).matches()) {
            return TextNode.valueOf(REDACTED);
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            ObjectNode sanitized = object.objectNode();
            object.fields().forEachRemaining(field -> sanitized.set(field.getKey(), sanitize(field.getValue(), field.getKey())));
            return sanitized;
        } else if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            ArrayNode sanitized = array.arrayNode();
            int itemCount = Math.min(array.size(), MAX_COLLECTION_ITEMS);
            for (int index = 0; index < itemCount; index++) {
                sanitized.add(sanitize(array.get(index), null));
            }
            if (array.size() > MAX_COLLECTION_ITEMS) {
                sanitized.add("[TRUNCATED]");
            }
            return sanitized;
        } else if (node.isTextual() && node.textValue().length() > MAX_STRING_LENGTH) {
            return TextNode.valueOf("[TRUNCATED length=" + node.textValue().length() + "]");
        }
        return node;
    }
}