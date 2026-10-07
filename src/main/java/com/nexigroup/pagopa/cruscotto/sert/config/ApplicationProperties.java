package com.nexigroup.pagopa.cruscotto.sert.config;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to PagoPa Cruscotto Sert Backend.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
public class ApplicationProperties {


    @Setter
    private boolean enableCsrf;

    private final Password password = new Password();



    private final AuthGroup authGroup = new AuthGroup();

    @Setter
    @Getter
    public static class Password {

        private Integer dayPasswordExpired;

        private Integer failedLoginAttempts;

        private Integer hoursKeyResetPasswordExpired;
    }

    @Setter
    @Getter
    public static class AuthGroup {

        private String superAdmin;
    }
}
