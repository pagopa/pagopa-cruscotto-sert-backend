package com.nexigroup.pagopa.cruscotto.sert.security.jwt;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String url;
    private String error;
    private String message;

    public String toJson() {
        return new StringJoiner(", ", "{", "}")
            .add("\"timestamp\": \"" + timestamp + "\"")
            .add("\"status\": " + status)
            .add("\"error\": \"" + error + "\"")
            .add("\"message\": \"" + message + "\"")
            .add("\"url\": \"" + url + "\"")
            .toString();
    }
}
