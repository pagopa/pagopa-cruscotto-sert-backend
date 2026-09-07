package com.nexigroup.pagopa.cruscotto.sert.domain.enumeration;

import java.util.Locale;

public enum SearchInstanceStatus {
    DRAFT, READY, RUNNING, EXECUTED, FAILED, ARCHIVED;

    public static SearchInstanceStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "DRAFT":
                return DRAFT;
            case "READY":
                return READY;
            case "RUNNING":
                return RUNNING;
            case "FAILED":
                return FAILED;
            case "EXECUTED":
                return EXECUTED;
            case "ARCHIVED":
                return ARCHIVED;


            default:
                throw new IllegalArgumentException("Unsupported PerimeterSearch status: " + value);
        }
    }
}
