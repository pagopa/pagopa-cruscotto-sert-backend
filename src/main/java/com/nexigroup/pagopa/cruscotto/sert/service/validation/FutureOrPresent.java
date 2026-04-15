package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Constraint(validatedBy = FutureOrPresentValidator.class)
public @interface FutureOrPresent {
	
    String message() default "{com.nexigroup.pagopa.cruscotto.service.validation.constraints.Future.message}";

    String date();

    String field();

    String pattern() default "dd-MM-yyyy";
    
    boolean present() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @interface List {
        FutureOrPresent[] value();
    }
}
