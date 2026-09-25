package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

/** Centralized keys and text for CSV validation errors. */
public enum CsvValidationMessage {
    MISSING_HEADER("CSV_HEADER_MISSING", "Non compare l'intestazione CSV"),
    UNKNOWN_HEADER("CSV_HEADER_UNKNOWN", "Intestazione CSV non riconosciuta"),
    UNEXPECTED_COLUMN("CSV_HEADER_COLUMN_UNKNOWN", "Colonna non prevista nell'intestazione"),
    DUPLICATE_COLUMN("CSV_HEADER_COLUMN_DUPLICATE", "Colonna duplicata nell'intestazione"),
    EMPTY_ROW("CSV_ROW_EMPTY", "La riga è vuota"),
    NO_DATA_ROWS("CSV_DATA_MISSING", "Non sono presenti righe dati"),
    COLUMN_COUNT("CSV_COLUMN_COUNT", "Numero di colonne non valido: previsto %d, trovato %d"),
    MISSING_VALUE("CSV_VALUE_MISSING", "Valore obbligatorio mancante"),
    INVALID_LENGTH("CSV_VALUE_LENGTH", "Lunghezza del valore non valida: prevista %d, trovata %d"),
    INVALID_TOKEN("CSV_TOKEN_INVALID", "Token non valido: i primi 32 caratteri devono essere un UUID senza trattini; il suffisso può avere al massimo 3 caratteri");

    private final String key;
    private final String text;

    CsvValidationMessage(String key, String text) {
        this.key = key;
        this.text = text;
    }

    public String key() {
        return key;
    }

    public String text() {
        return text;
    }

    public String format(Object... values) {
        return key + ": " + text.formatted(values);
    }
}