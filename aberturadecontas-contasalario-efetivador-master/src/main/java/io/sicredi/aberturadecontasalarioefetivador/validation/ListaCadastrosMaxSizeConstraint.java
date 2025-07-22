package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ListaCadastrosMaxSizeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListaCadastrosMaxSizeConstraint {

    String message() default "A solicitação não pode exceder 300 cadastros";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}