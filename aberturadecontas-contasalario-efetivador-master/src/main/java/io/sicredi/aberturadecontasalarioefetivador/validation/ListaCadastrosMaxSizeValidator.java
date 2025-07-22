package io.sicredi.aberturadecontasalarioefetivador.validation;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ListaCadastrosMaxSizeValidator implements ConstraintValidator<ListaCadastrosMaxSizeConstraint, List<CadastroRequestDTO>> {
    @Override
    public boolean isValid(List<CadastroRequestDTO> cadastros, ConstraintValidatorContext context) {
        if (cadastros != null && cadastros.size() > 300) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A solicitação não pode exceder 300 cadastros")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
