package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ListaCadastroCPFUnicoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListaCadastrosCPFUnicoConstraint {

    String message() default "A solicitação não pode possuir mais de um cadastro com o mesmo CPF";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
