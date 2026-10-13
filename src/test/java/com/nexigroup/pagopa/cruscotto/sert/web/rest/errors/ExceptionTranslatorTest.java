package com.nexigroup.pagopa.cruscotto.sert.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.config.RequestLoggingFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
}