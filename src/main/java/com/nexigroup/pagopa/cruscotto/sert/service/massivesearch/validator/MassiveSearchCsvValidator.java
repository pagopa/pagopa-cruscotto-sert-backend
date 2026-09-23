package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.*;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplateDetector.Field;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplateDetector.TemplateDetection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validates an uploaded Massive Search CSV and detects its template, streaming the source line by
 * line so large files are never loaded fully in memory.
 *
 * <p>This step only validates and normalizes rows; it does not generate any report.</p>
 */
@Slf4j
@Service
public class MassiveSearchCsvValidator {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9A-Fa-f]{32}.{0,3}");

    private final CsvTemplateDetector templateDetector;
    private final CsvInputReader inputReader;
    private final int maxErrors = CsvConfiguration.maxValidationErrors;

    public MassiveSearchCsvValidator(
        CsvTemplateDetector templateDetector,
        CsvInputReader inputReader
    ) {
        this.templateDetector = templateDetector;
        this.inputReader = inputReader;
   }

    /** Validates the CSV without retaining rows. */
    public CsvValidationResult validate(InputStream inputStream) {
        return validate(inputStream, null);
    }

    /**
     * Validates the CSV and, when a consumer is provided, streams each valid normalized row to it.
     *
     * @param inputStream      the CSV source (closed by the caller)
     * @return the validation result
     */

    public CsvTemplate extractCsvTemplate(InputStream inputStream) {
        BufferedReader reader = inputReader.newReader(inputStream);

        try {
            String headerLine = readHeaderLine(reader);
            if (headerLine == null || inputReader.isBlank(headerLine)) {
                throw new IllegalArgumentException(CsvValidationMessage.MISSING_HEADER.format());
            }
            List<String> headerColumns = inputReader.parseLine(headerLine);
            TemplateDetection detection = templateDetector.detect(headerColumns);
            if (detection.template() == CsvTemplate.UNKNOWN) {
                throw new IllegalArgumentException(CsvValidationMessage.UNKNOWN_HEADER.format());
            }
            return detection.template();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to extract CSV template", e);
        }


    }

    public CsvValidationResult validate(InputStream inputStream, Consumer<SearchInputRow> validRowConsumer) {
        List<CsvValidationError> errors = new ArrayList<>();

        try {
            BufferedReader reader = inputReader.newReader(inputStream);

            String headerLine = readHeaderLine(reader);
            if (headerLine == null || inputReader.isBlank(headerLine)) {
                addError(errors, CsvValidationError.row(1, CsvValidationMessage.MISSING_HEADER.format()));
                return invalidResult(CsvTemplate.UNKNOWN, errors);
            }

            List<String> headerColumns = inputReader.parseLine(headerLine);
            TemplateDetection detection = templateDetector.detect(headerColumns);
            boolean headerValid = validateHeader(detection, errors);

            if (detection.template() == CsvTemplate.UNKNOWN) {
                addError(errors, CsvValidationError.row(1, CsvValidationMessage.UNKNOWN_HEADER.format()));
                return invalidResult(CsvTemplate.UNKNOWN, errors);
            }

            Set<Field> requiredFields = requiredFields(detection.template());
            int expectedColumnCount = requiredFields.size();

            long totalRows = 0;
            long validRows = 0;
            long invalidRows = 0;
            String line;
            long lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (inputReader.isBlank(line)) {
                    totalRows++;
                    invalidRows++;
                    addError(errors, CsvValidationError.row(lineNumber, CsvValidationMessage.EMPTY_ROW.format()));
                    continue;
                }

                totalRows++;
                List<String> columns = inputReader.parseLine(line);
                if (columns.size() != expectedColumnCount) {
                    invalidRows++;
                    addError(errors, CsvValidationError.row(lineNumber,
                        CsvValidationMessage.COLUMN_COUNT.format(expectedColumnCount, columns.size())));
                    continue;
                }

                SearchInputRow row = inputReader.toRow(detection, columns);
                List<CsvValidationError> rowErrors = validateRow(lineNumber, requiredFields, row);
                if (rowErrors.isEmpty()) {
                    validRows++;
                    if (validRowConsumer != null) {
                        validRowConsumer.accept(row);
                    }
                } else {
                    invalidRows++;
                    rowErrors.forEach(e -> addError(errors, e));
                }
            }

            if (totalRows == 0) {
                addError(errors, CsvValidationError.row(1, CsvValidationMessage.NO_DATA_ROWS.format()));
            }
            boolean valid = headerValid && invalidRows == 0 && totalRows > 0;
            log.info("phase=CSV_VALIDATED template={} valid={} totalRows={} validRows={} invalidRows={} errors={}",
                detection.template(), valid, totalRows, validRows, invalidRows, errors.size());
            return new CsvValidationResult(valid, detection.template(), totalRows, validRows, invalidRows, errors);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read Massive Search CSV", e);
        }
    }

    private CsvValidationResult invalidResult(CsvTemplate template, List<CsvValidationError> errors) {
        log.info("phase=CSV_VALIDATED template={} valid=false totalRows=0 validRows=0 invalidRows=0 errors={}",
            template, errors.size());
        return new CsvValidationResult(false, template, 0, 0, 0, errors);
    }

    private String readHeaderLine(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    private boolean validateHeader(TemplateDetection detection, List<CsvValidationError> errors) {
        boolean valid = true;
        for (String unexpected : detection.unexpectedColumns()) {
            valid = false;
            addError(errors, CsvValidationError.column(1, unexpected,
                CsvValidationMessage.UNEXPECTED_COLUMN.format()));
        }
        for (String duplicate : detection.duplicateColumns()) {
            valid = false;
            addError(errors, CsvValidationError.column(1, duplicate,
                CsvValidationMessage.DUPLICATE_COLUMN.format()));
        }
        return valid;
    }

    private List<CsvValidationError> validateRow(
        long lineNumber,
        Set<Field> requiredFields,
        SearchInputRow row
    ) {
        List<CsvValidationError> rowErrors = new ArrayList<>();
        for (Field field : requiredFields) {
            String value = valueOf(field, row);
            String column = columnName(field);
            if (value == null) {
                rowErrors.add(CsvValidationError.column(lineNumber, column,
                    CsvValidationMessage.MISSING_VALUE.format()));
            } else if (field == Field.TOKEN) {
                if (!isValidToken(value)) {
                    rowErrors.add(CsvValidationError.column(lineNumber, column,
                        CsvValidationMessage.INVALID_TOKEN.format()));
                }
            } else {
                int expectedLength = CsvColumnLength.forField(field);
                if (value.length() != expectedLength) {
                    rowErrors.add(CsvValidationError.column(lineNumber, column,
                        CsvValidationMessage.INVALID_LENGTH.format(expectedLength, value.length())));
                }
            }
        }
        return rowErrors;
    }

    private boolean isValidToken(String value) {
        if (value.length() < CsvColumnLength.TOKEN_MIN_LENGTH
            || value.length() > CsvColumnLength.TOKEN_MAX_LENGTH
            || !TOKEN_PATTERN.matcher(value).matches()) {
            return false;
        }

        String baseUuid = value.substring(0, CsvColumnLength.TOKEN_MIN_LENGTH);
        String uuidWithDashes = baseUuid.substring(0, 8) + "-"
            + baseUuid.substring(8, 12) + "-"
            + baseUuid.substring(12, 16) + "-"
            + baseUuid.substring(16, 20) + "-"
            + baseUuid.substring(20);
        try {
            UUID.fromString(uuidWithDashes);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String columnName(Field field) {
        return field == Field.PA ? "EC" : field.name();
    }

    private String valueOf(Field field, SearchInputRow row) {
        return switch (field) {
            case NAV -> row.nav();
            case PA -> row.pa();
            case IUV -> row.iuv();
            case TOKEN -> row.token();
        };
    }

    private Set<Field> requiredFields(CsvTemplate template) {
        return switch (template) {
            case NAV_PA -> EnumSet.of(Field.NAV, Field.PA);
            case IUV_PA -> EnumSet.of(Field.IUV, Field.PA);
            case NAV -> EnumSet.of(Field.NAV);
            case IUV -> EnumSet.of(Field.IUV);
            case TOKEN -> EnumSet.of(Field.TOKEN);
            case UNKNOWN -> Set.of();
        };
    }

    private void addError(List<CsvValidationError> errors, CsvValidationError error) {
        if (errors.size() < maxErrors) {
            errors.add(error);
        }
    }
}
