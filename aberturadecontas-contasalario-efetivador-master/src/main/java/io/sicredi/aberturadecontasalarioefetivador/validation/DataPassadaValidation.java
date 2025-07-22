package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DataPassadaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPassadaValidation {

    String message() default "O campo deve estar ser uma data passada";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
