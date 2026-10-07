package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvInputReaderTest {

    private final CsvInputReader reader = new CsvInputReader();

    @Test
    void parsesSupportedSeparatorsAndQuotedEscapedValues() {
        assertThat(reader.parseLine("\"alpha;beta\";\"say \"\"hello\"\"\";gamma"))
            .containsExactly("alpha;beta", "say \"hello\"", "gamma");
        assertThat(reader.parseLine("one,two:three")).containsExactly("one", "two", "three");
    }

    @Test
    void normalizesCellsByDetectedTemplateAndTreatsBlanksAsNull() {
        var detection = new CsvTemplateDetector().detect(List.of("NAV", "EC"));
        SearchInputRow row = reader.toRow(detection, List.of(" 123456789012345678 ", "\uFEFF 12345678901 "));
        SearchInputRow emptyRow = reader.toRow(detection, List.of(" ", ""));

        assertThat(row).isEqualTo(new SearchInputRow("123456789012345678", "12345678901", null, null));
        assertThat(emptyRow.nav()).isNull();
        assertThat(emptyRow.pa()).isNull();
        assertThat(reader.isBlank(null)).isTrue();
        assertThat(reader.isBlank(" \t")).isTrue();
        assertThat(reader.isBlank("data")).isFalse();
        assertThat(reader.newReader((String) null).lines()).isEmpty();
    }
}