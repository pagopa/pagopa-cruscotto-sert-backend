package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void putsRequestIdentifiersInMdcAndRestoresContextAfterRequest() throws Exception {
        String transactionId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestLoggingFilter.TRANSACTION_ID_HEADER, transactionId);
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertThat(MDC.get(RequestLoggingFilter.TRANSACTION_ID_MDC_KEY)).isEqualTo(transactionId);
            assertThat(MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY)).isEqualTo(correlationId);
        });

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).isEqualTo(correlationId);
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void generatesCorrelationIdAndRejectsInvalidTransactionId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestLoggingFilter.TRANSACTION_ID_HEADER, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertThat(MDC.get(RequestLoggingFilter.TRANSACTION_ID_MDC_KEY)).isNull();
            assertThat(MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY)).isEqualTo(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER));
            assertThat(UUID.fromString(MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY))).isNotNull();
        });

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).isNotBlank();
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void logsOnlyTraceIdFromValidW3cTraceparent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");

        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) ->
            assertThat(MDC.get(RequestLoggingFilter.TRACE_ID_MDC_KEY)).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736")
        );

        assertThat(MDC.get(RequestLoggingFilter.TRACE_ID_MDC_KEY)).isNull();
    }
}