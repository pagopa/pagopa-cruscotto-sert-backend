package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

/**
 * A single validation problem detected while parsing a Massive Search CSV.
 *
 * @param lineNumber 1-based line number in the source file ({@code 1} = header)
 * @param column     the offending column name, or {@code null} when the problem is row-level
 * @param message    human-readable description of the problem
 */
public record CsvValidationError(long lineNumber, String column, String codeMessage , String message) {

    public static CsvValidationError row(long lineNumber,CsvValidationMessage csvValidator ) {
        return new CsvValidationError(lineNumber, null, csvValidator.key(),csvValidator.text());
    }

    public static CsvValidationError column(long lineNumber, String column,CsvValidationMessage csvValidator) {
        return new CsvValidationError(lineNumber, column,csvValidator.key(),csvValidator.text());
    }

    public static CsvValidationError rowKeyText(long lineNumber, String key, String text) {
        return new CsvValidationError(lineNumber, null, key, text);
    }

    public static CsvValidationError columnKeyText(long lineNumber, String column, String key, String text) {
        return new CsvValidationError(lineNumber, column, key, text);
    }
}
