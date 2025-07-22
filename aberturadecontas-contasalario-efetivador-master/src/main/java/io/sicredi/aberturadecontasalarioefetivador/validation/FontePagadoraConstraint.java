package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { FontePagadoraValidator.class })
@Documented
public @interface FontePagadoraConstraint {

    String message() default "Apenas um dos campos 'cnpjFontePagadora' ou 'cpfFontePagadora' deve ser informado, e ao menos um é obrigatório";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
