package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CsvConfiguration {
    public static  char separator = ',';
    public static  Charset charset = StandardCharsets.UTF_8;
    public static  int maxRows = 500000;
    public static  int maxValidationErrors = 1000;
}
