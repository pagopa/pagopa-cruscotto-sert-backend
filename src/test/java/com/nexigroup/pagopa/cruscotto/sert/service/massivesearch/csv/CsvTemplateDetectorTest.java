package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvTemplateDetectorTest {

    private final CsvTemplateDetector detector = new CsvTemplateDetector();

    @Test
    void recognizesTemplatesIgnoringCaseWhitespaceAndUtf8Bom() {
        CsvTemplateDetector.TemplateDetection navPa = detector.detect(List.of("\uFEFF NAV ", "eC"));
        CsvTemplateDetector.TemplateDetection iuvPa = detector.detect(List.of(" IUV", "EC "));
        CsvTemplateDetector.TemplateDetection token = detector.detect(List.of("Token"));

        assertThat(navPa.template()).isEqualTo(CsvTemplate.NAV_PA);
        assertThat(navPa.navIndex()).isZero();
        assertThat(navPa.paIndex()).isEqualTo(1);
        assertThat(iuvPa.template()).isEqualTo(CsvTemplate.IUV_PA);
        assertThat(iuvPa.iuvIndex()).isZero();
        assertThat(iuvPa.paIndex()).isEqualTo(1);
        assertThat(token.template()).isEqualTo(CsvTemplate.TOKEN);
        assertThat(token.tokenIndex()).isZero();
    }

    @Test
    void reportsDuplicateUnexpectedAndBlankHeadersWithoutReplacingFirstIndex() {
        CsvTemplateDetector.TemplateDetection result = detector.detect(Arrays.asList("NAV", "nav", "extra", null, "EC"));

        assertThat(result.template()).isEqualTo(CsvTemplate.NAV_PA);
        assertThat(result.navIndex()).isZero();
        assertThat(result.paIndex()).isEqualTo(4);
        assertThat(result.duplicateColumns()).containsExactly("nav");
        assertThat(result.unexpectedColumns()).containsExactly("extra", "");
    }

    @Test
    void returnsUnknownForEmptyOrUnrecognizedHeaders() {
        assertThat(detector.detect(null).template()).isEqualTo(CsvTemplate.UNKNOWN);
        assertThat(detector.detect(List.of()).template()).isEqualTo(CsvTemplate.UNKNOWN);
        assertThat(detector.detect(List.of("PAYMENT", "CODE")).template()).isEqualTo(CsvTemplate.UNKNOWN);
    }
}