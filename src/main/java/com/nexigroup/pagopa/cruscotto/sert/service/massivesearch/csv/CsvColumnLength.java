package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplateDetector.Field;

/** Configurable expected lengths for the fields accepted by massive-search CSV templates. */
public final class CsvColumnLength {

    public static int NAV_LENGTH = 18;
    public static int IUV_LENGTH = 18;
    public static int DOMINIO_LENGTH = 11;
    public static int TOKEN_MIN_LENGTH = 32;
    public static int TOKEN_MAX_LENGTH = 35;

    private CsvColumnLength() {
    }

    public static int forField(Field field) {
        return switch (field) {
            case NAV -> NAV_LENGTH;
            case IUV -> IUV_LENGTH;
            case PA -> DOMINIO_LENGTH;
            case TOKEN -> TOKEN_MIN_LENGTH;
        };
    }
}