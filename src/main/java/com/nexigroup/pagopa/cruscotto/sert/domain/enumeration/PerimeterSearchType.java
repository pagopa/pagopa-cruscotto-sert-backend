package com.nexigroup.pagopa.cruscotto.sert.domain.enumeration;

import java.util.Locale;

public enum PerimeterSearchType {
    FILTER ,
    CSV;


    public static PerimeterSearchType fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "FILTER":
                return FILTER;
            case "CSV":
                return CSV;
            default:
                throw new IllegalArgumentException("Unsupported PerimeterSearch status: " + value);
        }
    }
}


