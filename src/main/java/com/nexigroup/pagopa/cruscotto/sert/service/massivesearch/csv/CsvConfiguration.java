package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvConfiguration {
    public static  List<Character> separator = List.of(',',';',':');
    public static  Charset charset = StandardCharsets.UTF_8;
    public static  int maxRows = 500000;
    public static  int maxValidationErrors = 1000;
}
