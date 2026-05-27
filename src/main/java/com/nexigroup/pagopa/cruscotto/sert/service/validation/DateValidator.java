package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class DateValidator implements ConstraintValidator<ValidDate, Object> {

    private final Logger logger = LoggerFactory.getLogger(DateValidator.class);

    private String pattern;
    
    @Override
    public void initialize(ValidDate constraintAnnotation) {
    	pattern = constraintAnnotation.pattern();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        
    	try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            
            if(value != null && !value.toString().isEmpty() ) {
            	
                sdf.parse(value.toString());
                return true;                               
            }
            return true;
        }
        catch(ParseException parseException) {
            logger.error("can not parse date", parseException);

            return false;
        }  catch(Exception exception) {
            logger.error("can not validate date", exception);

            return false;
        }
    }
}
