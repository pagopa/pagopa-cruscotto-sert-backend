package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class RangeDateValidator implements ConstraintValidator<ValidRangeDate, Object> {

    private final Logger logger = LoggerFactory.getLogger(RangeDateValidator.class);

    private String minDate;
    private String pattern;
    private String maxDate;
    private boolean equalsIsValid;

    @Override
    public void initialize(ValidRangeDate constraintAnnotation) {
        minDate = constraintAnnotation.minDate();
        maxDate = constraintAnnotation.maxDate();
        pattern = constraintAnnotation.pattern();
        equalsIsValid = constraintAnnotation.equalsIsValid();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        
    	boolean result = true;
    	
    	try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            String strMinDate = BeanUtils.getProperty(value, minDate);
            String strMaxDate = BeanUtils.getProperty(value, maxDate);

            if(strMinDate != null && strMaxDate != null) {
                Date dateMin = sdf.parse(strMinDate);
                Date dateMax = sdf.parse(strMaxDate);
                
                result = dateMin.before(dateMax);
                
                if(!result && equalsIsValid) {
                	result = dateMin.equals(dateMax);
                }               
            }
            return result;
        }
        catch(ParseException parseException) {
            logger.error("Problemi formattazione date");

            return true;
        }  catch(Exception exception) {
            logger.error("Problemi validazione date");

            return true;
        }
    }
}
