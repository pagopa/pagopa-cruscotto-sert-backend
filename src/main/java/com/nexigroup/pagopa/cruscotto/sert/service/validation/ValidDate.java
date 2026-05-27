package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@Constraint(validatedBy = DateValidator.class)
public @interface ValidDate {
    String message() default "{com.nexigroup.pagopa.cruscotto.service.validation.constraints.ValidDate.message}";

    String pattern() default "dd-MM-yyyy";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
