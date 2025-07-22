package io.sicredi.aberturadecontasalarioefetivador.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;

public class FontePagadoraValidator implements ConstraintValidator<FontePagadoraConstraint, SolicitacaoRequestDTO> {

    @Override
    public boolean isValid(SolicitacaoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        String cnpj = dto.cnpjFontePagadora();
        String cpf = dto.cpfFontePagadora();

        boolean cnpjPreenchido = StringUtils.isNotBlank(cnpj);
        boolean cpfPreenchido = StringUtils.isNotBlank(cpf);

        if (!cnpjPreenchido && !cpfPreenchido) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "É necessário informar 'cnpjFontePagadora' ou 'cpfFontePagadora'")
                    .addPropertyNode("fontePagadora")
                    .addConstraintViolation();
            return false;
        }

        if (cnpjPreenchido && cpfPreenchido) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Apenas um dos campos 'cnpjFontePagadora' ou 'cpfFontePagadora' deve ser informado, não ambos")
                    .addPropertyNode("fontePagadora")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}