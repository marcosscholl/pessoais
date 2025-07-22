package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataEmFormatoValidoValidator implements ConstraintValidator<DataEmFormatoValidoValidation, String> {

    @Override
    public boolean isValid(String data, ConstraintValidatorContext context) {
        try {
            if(Strings.isNotBlank(data)){
                LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                return true;
            }
            return true;
        }
        catch (DateTimeParseException e){
            return false;
        }
    }
}