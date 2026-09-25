package com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv;

/**
 * Supported Massive Search CSV templates. Mirrors the {@code search_perimeter_file.template} domain.
 */
public enum CsvTemplate {
    /** NAV + PA. */
    NAV_PA,
    /** IUV + PA. */
    IUV_PA,
    /** Only NAV. */
    NAV,
    /** Only IUV. */
    IUV,
    /** Only TOKEN. */
    TOKEN,
    /** Header not recognized. */
    UNKNOWN
}
