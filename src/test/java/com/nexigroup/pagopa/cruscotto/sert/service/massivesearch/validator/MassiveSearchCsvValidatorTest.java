package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvInputReader;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplate;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplateDetector;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MassiveSearchCsvValidatorTest {

    private MassiveSearchCsvValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MassiveSearchCsvValidator(new CsvTemplateDetector(), new CsvInputReader());
    }

    @Test
    void validatesNavEcCsvAndStreamsOnlyValidRows() {
        String csv = "NAV;EC\n123456789012345678;12345678901\nshort;12345678901\n";
        List<Object> rows = new ArrayList<>();

        var result = validator.validate(input(csv), rows::add);

        assertThat(result.valid()).isFalse();
        assertThat(result.detectedTemplate()).isEqualTo(CsvTemplate.NAV_PA);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.validRows()).isEqualTo(1);
        assertThat(result.invalidRows()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(rows).hasSize(1);
    }

    @Test
    void rejectsMissingAndUnknownHeadersAndHeaderOnlyInput() {
        var missing = validator.validate(input("  \n"));
        var unknown = validator.validate(input("PAYMENT;CODE\n"));
        var noRows = validator.validate(input("NAV\n"));

        assertThat(missing.valid()).isFalse();
        assertThat(missing.errors()).extracting("codeMessage").contains("CSV_HEADER_MISSING");
        assertThat(unknown.valid()).isFalse();
        assertThat(unknown.detectedTemplate()).isEqualTo(CsvTemplate.UNKNOWN);
        assertThat(noRows.valid()).isFalse();
        assertThat(noRows.errors()).extracting("codeMessage").contains("CSV_DATA_MISSING");
    }

    @Test
    void detectsTemplateAndRejectsUnsupportedTemplate() {
        assertThat(validator.extractCsvTemplate(input("IUV;EC\n"))).isEqualTo(CsvTemplate.IUV_PA);
        assertThatThrownBy(() -> validator.extractCsvTemplate(input("unknown\n")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Intestazione CSV non riconosciuta");
    }

    @Test
    void rejectsInvalidTokenAndWrongNumberOfColumns() {
        var result = validator.validate(input("TOKEN\nnot-a-token\n" +
            "12345678123412341234123412345678;extra\n"));

        assertThat(result.valid()).isFalse();
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.invalidRows()).isEqualTo(2);
        assertThat(result.errors()).extracting("codeMessage")
            .contains("CSV_TOKEN_INVALID", "CSV_COLUMN_COUNT");
    }

    private ByteArrayInputStream input(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}