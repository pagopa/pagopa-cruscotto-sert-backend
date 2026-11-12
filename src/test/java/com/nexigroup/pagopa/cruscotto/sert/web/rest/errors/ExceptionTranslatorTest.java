package com.nexigroup.pagopa.cruscotto.sert.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.config.RequestLoggingFilter;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.HistoryDatabaseUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.request.ServletWebRequest;

class ExceptionTranslatorTest {

    private final ExceptionTranslator exceptionTranslator = new ExceptionTranslator(new MockEnvironment());

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void includesCorrelationIdInProblemProperties() {
        String correlationId = "cfd0b210-3724-4339-a126-e421f45b2132";
        MDC.put(RequestLoggingFilter.CORRELATION_ID_MDC_KEY, correlationId);
        ProblemDetailWithCause problem = ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
            .withStatus(HttpStatus.BAD_REQUEST.value())
            .build();
        MockHttpServletRequest request = new MockHttpServletRequest();

        exceptionTranslator.customizeProblem(
            problem,
            new IllegalArgumentException(),
            new ServletWebRequest(request, new MockHttpServletResponse())
        );

        assertThat(problem.getProperties()).containsEntry("correlationId", correlationId);
    }

    @Test
    void respondsWithServiceUnavailableWhenHistoryDatabaseIsUnavailable() {
        ResponseEntity<Object> response = exceptionTranslator.handleAnyException(
            new HistoryDatabaseUnavailableException(new IllegalStateException("connection failed")),
            new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isInstanceOf(ProblemDetailWithCause.class);
        ProblemDetailWithCause problem = (ProblemDetailWithCause) response.getBody();
        assertThat(problem.getDetail()).isEqualTo("DB History unavailable");
    }
}