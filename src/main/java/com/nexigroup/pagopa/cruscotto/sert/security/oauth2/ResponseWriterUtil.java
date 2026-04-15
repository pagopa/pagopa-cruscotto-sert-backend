package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseWriterUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseWriterUtil.class);

    public static void writeResponse(HttpServletResponse response, String message) {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(message);
            writer.flush();
        } catch (IOException ie) {
            LOG.error("Problem writing output to response!", ie);
        }
    }

    public static void writeErrorResponse(HttpServletResponse response, String message) {
        ResponseStatus status = new ResponseStatus();
        status.setStatusCode(ResponseStatus.StatusCode.ERROR);
        status.setMessage(message);

        try (PrintWriter writer = response.getWriter()) {
            String json = new ObjectMapper().writeValueAsString(status);

            writer.write(json);
            writer.flush();
        } catch (IOException ie) {
            LOG.error("Problem writing output to response!", ie);
        }
    }
}
