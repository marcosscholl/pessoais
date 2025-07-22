package io.sicredi.aberturadecontasalarioefetivador.validation;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TitularRepresentanteValidator implements ConstraintValidator<TitularRepresentanteConstraint, CadastroRequestDTO> {

    @Override
    public boolean isValid(CadastroRequestDTO cadastro, ConstraintValidatorContext context) {
        if (cadastroComRepresentante(cadastro) && titularRepresentante(cadastro)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("O titular não pode ser o seu próprio representante")
                    .addPropertyNode("representante")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private static boolean titularRepresentante(CadastroRequestDTO cadastro) {
        return cadastro.cpf().equals(cadastro.representante().cpf());
    }

    private static boolean cadastroComRepresentante(CadastroRequestDTO cadastro) {
        return cadastro != null && cadastro.representante() != null && cadastro.representante().cpf() != null;
    }
}