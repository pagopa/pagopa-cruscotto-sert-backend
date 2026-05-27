package com.nexigroup.pagopa.cruscotto.sert.security.util;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class PasswordExpiredUtils {

    private PasswordExpiredUtils() {}

    public static boolean isPasswordNonExpired(ZonedDateTime lastPasswordChangeDate, Integer passwordExpiredDay) {
        boolean isNonExpired = true;
        if (passwordExpiredDay != null && lastPasswordChangeDate != null) {
            LocalDate dateLastChangedPassword = lastPasswordChangeDate.toLocalDate().plusDays(passwordExpiredDay);
            LocalDate now = LocalDate.now();
            if (now.isEqual(dateLastChangedPassword) || now.isAfter(dateLastChangedPassword)) isNonExpired = false;
        }

        return isNonExpired;
    }

    public static LocalDate getPasswordExpiredDate(ZonedDateTime lastPasswordChangeDate, Integer passwordExpiredDay) {
        LocalDate passwordExpiredDate = null;

        if (passwordExpiredDay != null && lastPasswordChangeDate != null) {
            LocalDate dateLastChangedPassword = lastPasswordChangeDate.toLocalDate();
            passwordExpiredDate = dateLastChangedPassword.plusDays(passwordExpiredDay);
        }

        return passwordExpiredDate;
    }
}
