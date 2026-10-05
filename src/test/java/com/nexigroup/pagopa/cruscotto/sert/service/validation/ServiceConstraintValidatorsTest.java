package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class ServiceConstraintValidatorsTest {

    private enum SampleStatus { ACTIVE, CLOSED }

    @Test
    void validatesDateAndEnumValues() {
        ValidDate validDate = mock(ValidDate.class);
        when(validDate.pattern()).thenReturn("dd-MM-yyyy");
        DateValidator dateValidator = new DateValidator();
        dateValidator.initialize(validDate);
        assertThat(dateValidator.isValid("05-10-2026", null)).isTrue();
        assertThat(dateValidator.isValid("not-a-date", null)).isFalse();
        assertThat(dateValidator.isValid(null, null)).isTrue();

        ValidEnum validEnum = mock(ValidEnum.class);
        doReturn(SampleStatus.class).when(validEnum).enumClass();
        when(validEnum.ignoreCase()).thenReturn(false);
        EnumValueValidator enumValidator = new EnumValueValidator();
        enumValidator.initialize(validEnum);
        assertThat(enumValidator.isValid("ACTIVE", null)).isTrue();
        assertThat(enumValidator.isValid("active", null)).isFalse();
        assertThat(enumValidator.isValid(" ", null)).isTrue();

        when(validEnum.ignoreCase()).thenReturn(true);
        enumValidator.initialize(validEnum);
        assertThat(enumValidator.isValid("active", null)).isTrue();
        assertThat(enumValidator.isValid("unknown", null)).isFalse();
    }

    @Test
    void validatesDateRangeAndFutureOrPresentBoundaries() {
        ValidRangeDate validRange = mock(ValidRangeDate.class);
        when(validRange.minDate()).thenReturn("from");
        when(validRange.maxDate()).thenReturn("to");
        when(validRange.pattern()).thenReturn("yyyy-MM-dd");
        when(validRange.equalsIsValid()).thenReturn(false);
        RangeDateValidator rangeValidator = new RangeDateValidator();
        rangeValidator.initialize(validRange);
        assertThat(rangeValidator.isValid(new DateRange("2026-01-01", "2026-01-02"), null)).isTrue();
        assertThat(rangeValidator.isValid(new DateRange("2026-01-02", "2026-01-01"), null)).isFalse();
        when(validRange.equalsIsValid()).thenReturn(true);
        rangeValidator.initialize(validRange);
        assertThat(rangeValidator.isValid(new DateRange("2026-01-01", "2026-01-01"), null)).isTrue();

        FutureOrPresent futureOrPresent = mock(FutureOrPresent.class);
        when(futureOrPresent.date()).thenReturn("date");
        when(futureOrPresent.pattern()).thenReturn("yyyy-MM-dd");
        when(futureOrPresent.present()).thenReturn(false);
        FutureOrPresentValidator futureValidator = new FutureOrPresentValidator();
        futureValidator.initialize(futureOrPresent);
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(futureValidator.isValid(new DateRange(null, null, tomorrow), null)).isTrue();
        assertThat(futureValidator.isValid(new DateRange(null, null, today), null)).isFalse();
        when(futureOrPresent.present()).thenReturn(true);
        futureValidator.initialize(futureOrPresent);
        assertThat(futureValidator.isValid(new DateRange(null, null, today), null)).isTrue();
    }

    @Test
    void delegatesResourcePermissionDecisionToUserService() {
        AuthUserService userService = mock(AuthUserService.class);
        UserResourcePermissionValidator permissionValidator = new UserResourcePermissionValidator(userService);
        when(userService.getAbilitazioneUtenteLoggatoManageOtherUser("operator", 8L, null)).thenReturn(false);
        when(userService.getAbilitazioneUtenteLoggatoManageOtherUser("operator", null, "target")).thenReturn(true);

        assertThat(permissionValidator.userCanAccessIdUserResource("operator", 8L)).isTrue();
        assertThat(permissionValidator.userCanAccessUserResource("operator", "target")).isFalse();
    }

    public static class DateRange {

        private final String from;
        private final String to;
        private final String date;

        private DateRange(String from, String to) {
            this(from, to, null);
        }

        private DateRange(String from, String to, String date) {
            this.from = from;
            this.to = to;
            this.date = date;
        }

        public String getFrom() { return from; }

        public String getTo() { return to; }

        public String getDate() { return date; }
    }
}