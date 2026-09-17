package com.nexigroup.pagopa.cruscotto.sert.domain.enumeration;

import java.util.Locale;

public enum SelectedReports {
    POSITION,
    TOKEN,
    TRANSFER;

    public static SelectedReports fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "POSITION":
                return POSITION;
            case "TOKEN":
                return TOKEN;
            case "TRANSFER":
                return TRANSFER;
            default:
                throw new IllegalArgumentException("Unsupported PerimeterSearch status: " + value);
        }
    }
}
