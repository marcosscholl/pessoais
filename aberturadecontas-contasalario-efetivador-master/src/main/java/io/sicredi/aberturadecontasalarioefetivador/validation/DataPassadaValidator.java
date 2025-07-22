package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataPassadaValidator implements ConstraintValidator<DataPassadaValidation, String> {

    @Override
    public boolean isValid(String data, ConstraintValidatorContext context) {
        try {
            if(Strings.isNotBlank(data)){
                LocalDate localDate = LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                return localDate.isBefore(LocalDate.now());
            }
            return true;
        }
        catch (DateTimeParseException e){
            return false;
        }
    }
}