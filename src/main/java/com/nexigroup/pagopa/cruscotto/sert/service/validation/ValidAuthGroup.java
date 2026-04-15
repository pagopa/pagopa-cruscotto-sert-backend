package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { ValidAuthGroupValidator.class })
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAuthGroup {
    String message() default "{com.nexigroup.pagopa.cruscotto.service.validation.constraints.AuthGroup.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
