package com.nexigroup.pagopa.cruscotto.sert.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.springframework.web.multipart.MultipartFile;

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
        MDC.put("request", toJson(requestArguments(signature, joinPoint.getArgs())));
        LOG.info("API request started: {} correlationId={}", action, correlationId);

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
                MDC.put("event_action", action);
                MDC.put("event_outcome", failure == null && status < 400 ? "success" : "failure");
                MDC.put("http.response.status_code", Integer.toString(status));
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

    private Map<String, Object> requestArguments(MethodSignature signature, Object[] arguments) {
        String[] parameterNames = signature.getParameterNames();
        Map<String, Object> request = new LinkedHashMap<>();
        for (int index = 0; index < arguments.length; index++) {
            Object argument = arguments[index];
            if (argument == null || argument instanceof ServletRequest || argument instanceof ServletResponse || argument instanceof java.security.Principal) {
                continue;
            }

            String name = index < parameterNames.length ? parameterNames[index] : "arg" + index;
            request.put(name, argument instanceof MultipartFile file ? fileMetadata(file) : argument);
        }
        return request;
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

    private Map<String, Object> fileMetadata(MultipartFile file) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("fileName", file.getOriginalFilename());
        metadata.put("contentType", file.getContentType());
        metadata.put("sizeBytes", file.getSize());
        return metadata;
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