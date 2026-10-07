package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;

class CRLFLogConverterTest {

    private final TestableCRLFLogConverter converter = new TestableCRLFLogConverter();

    @Test
    void replacesNewlinesCarriageReturnsAndTabs() {
        ILoggingEvent event = event("application.service", null);

        assertThat(converter.applyTransform(event, "one\ntwo\rthree\tfour"))
            .isEqualTo("one_two_three_four");
    }

    @Test
    void leavesTrustedLoggerMessageUnchanged() {
        ILoggingEvent event = event("org.springframework.boot.autoconfigure.config", null);

        assertThat(converter.applyTransform(event, "one\ntwo")).isEqualTo("one\ntwo");
    }

    @Test
    void leavesMessageWithSafeMarkerUnchanged() {
        Marker marker = mock(Marker.class);
        when(marker.contains(CRLFLogConverter.CRLF_SAFE_MARKER)).thenReturn(true);
        ILoggingEvent event = event("application.service", List.of(marker));

        assertThat(converter.applyTransform(event, "one\ntwo")).isEqualTo("one\ntwo");
    }

    private ILoggingEvent event(String loggerName, List<Marker> markers) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getLoggerName()).thenReturn(loggerName);
        when(event.getMarkerList()).thenReturn(markers);
        return event;
    }

    private static class TestableCRLFLogConverter extends CRLFLogConverter {

        private String applyTransform(ILoggingEvent event, String message) {
            return super.transform(event, message);
        }
    }
}