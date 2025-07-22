package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TitularRepresentanteValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TitularRepresentanteConstraint {

    String message() default "Titular não pode ser o representante";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
