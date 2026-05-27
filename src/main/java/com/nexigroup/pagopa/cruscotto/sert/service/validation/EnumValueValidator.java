package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class EnumValueValidator implements ConstraintValidator<ValidEnum, String> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(String valueForValidation, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(valueForValidation)) return true;

        boolean result = false;

        Object[] enumValues = this.annotation.enumClass().getEnumConstants();

        if (enumValues != null) {
            for (Object enumValue : enumValues) {
                if (
                    valueForValidation.equals(enumValue.toString()) ||
                    (this.annotation.ignoreCase() && valueForValidation.equalsIgnoreCase(enumValue.toString()))
                ) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
