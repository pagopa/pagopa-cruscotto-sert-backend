package com.nexigroup.pagopa.cruscotto.sert.domain.enumeration;

import java.util.Locale;

public enum CustomerGeneratedFile {
    GENERATED_FROM_FILTERS,
    USER_UPLOADED;

    public static CustomerGeneratedFile fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "GENERATED_FROM_FILTERS":
                return GENERATED_FROM_FILTERS;
            case "READY":
                return USER_UPLOADED;
            case "RUNNING":
                return USER_UPLOADED;
            default:
                throw new IllegalArgumentException("Unsupported CustomerGeneratedFile status: " + value);
        }
    }
}
