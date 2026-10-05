package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class ResponseWriterUtilTest {

    @Test
    void writesPlainAndStructuredErrorResponses() throws Exception {
        MockHttpServletResponse plainResponse = new MockHttpServletResponse();
        ResponseWriterUtil.writeResponse(plainResponse, "plain response");
        assertThat(plainResponse.getContentAsString()).isEqualTo("plain response");

        MockHttpServletResponse errorResponse = new MockHttpServletResponse();
        ResponseWriterUtil.writeErrorResponse(errorResponse, "invalid request");
        assertThat(errorResponse.getContentAsString()).contains("ERROR", "invalid request");

        ResponseStatus status = new ResponseStatus();
        status.setStatusCode(ResponseStatus.StatusCode.OK);
        status.setMessage("complete");
        assertThat(status.getStatusCode()).isEqualTo(ResponseStatus.StatusCode.OK);
        assertThat(status.getMessage()).isEqualTo("complete");
    }
}