package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureOrPresentValidator implements ConstraintValidator<FutureOrPresent, Object> {

    private final Logger logger = LoggerFactory.getLogger(FutureOrPresentValidator.class);

    private String date;
    private String pattern;
    private boolean present;

    @Override
    public void initialize(FutureOrPresent constraintAnnotation) {
        date = constraintAnnotation.date();
        pattern = constraintAnnotation.pattern();
        present = constraintAnnotation.present();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean result = true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            String strDate = BeanUtils.getProperty(value, date);

            if (strDate != null) {
                Date dateToCheck = sdf.parse(strDate);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date currentDate = calendar.getTime();
                result = dateToCheck.after(currentDate);

                if (!result && present) {
                    result = dateToCheck.compareTo(currentDate) == 0;
                }
            }
            return result;
        } catch (ParseException parseException) {
            logger.error("Problemi formattazione date");

            return true;
        } catch (Exception exception) {
            logger.error("Problemi validazione date");

            return true;
        }
    }
}
