package com.nexigroup.pagopa.cruscotto.sert.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class EcsJsonLayout extends LayoutBase<ILoggingEvent> {

    private static final String BUILD_INFO_PATH = "/META-INF/build-info.properties";
    private static final String UNKNOWN_VALUE = "unknown";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String serviceVersion = UNKNOWN_VALUE;

    @Override
    public void start() {
        serviceVersion = loadServiceVersion();
        super.start();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        event.prepareForDeferredProcessing();

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("@timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeStamp())));
        fields.put("log.level", event.getLevel().toString());
        fields.put("service.name", contextProperty("service.name"));
        fields.put("service.version", serviceVersion);
        fields.put("environment", contextProperty("environment"));
        fields.put("log.logger", event.getLoggerName());
        fields.put("process.thread.name", event.getThreadName());
        fields.put("message", event.getFormattedMessage());

        event.getMDCPropertyMap().forEach((name, value) -> fields.put(name, structuredField(name, value)));
        addErrorFields(event.getThrowableProxy(), fields);

        try {
            return objectMapper.writeValueAsString(fields) + System.lineSeparator();
        } catch (JsonProcessingException exception) {
            addError("Failed to serialize log event: " + exception.getOriginalMessage());
            return "{\"log.level\":\"ERROR\",\"message\":\"Log event serialization failed\"}" + System.lineSeparator();
        }
    }

    private Object structuredField(String name, String value) {
        if (!"request".equals(name) && !"response".equals(name)) {
            return value;
        }

        try {
            JsonNode json = objectMapper.readTree(value);
            return json == null ? value : json;
        } catch (JsonProcessingException exception) {
            return value;
        }
    }

    private void addErrorFields(IThrowableProxy throwable, Map<String, Object> fields) {
        if (throwable == null) {
            return;
        }

        fields.put("error.type", throwable.getClassName());
        if (throwable.getMessage() != null) {
            fields.put("error.message", throwable.getMessage());
        }
        fields.put("error.stack_trace", ThrowableProxyUtil.asString(throwable));
    }

    private String contextProperty(String name) {
        Context context = getContext();
        if (context == null) {
            return UNKNOWN_VALUE;
        }

        String value = context.getProperty(name);
        return value == null || value.isBlank() ? UNKNOWN_VALUE : value;
    }

    private String loadServiceVersion() {
        try (InputStream stream = EcsJsonLayout.class.getResourceAsStream(BUILD_INFO_PATH)) {
            if (stream == null) {
                return UNKNOWN_VALUE;
            }

            Properties buildInfo = new Properties();
            buildInfo.load(stream);
            return buildInfo.getProperty("build.version", UNKNOWN_VALUE);
        } catch (IOException exception) {
            addError("Failed to read application build info: " + exception.getMessage());
            return UNKNOWN_VALUE;
        }
    }
}