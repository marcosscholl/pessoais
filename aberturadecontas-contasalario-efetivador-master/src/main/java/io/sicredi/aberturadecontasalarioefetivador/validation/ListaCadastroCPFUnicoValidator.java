package io.sicredi.aberturadecontasalarioefetivador.validation;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collections;
import java.util.List;

public class ListaCadastroCPFUnicoValidator implements ConstraintValidator<ListaCadastrosCPFUnicoConstraint, List<CadastroRequestDTO>> {
    @Override
    public boolean isValid(List<CadastroRequestDTO> cadastros, ConstraintValidatorContext context) {
        if (cadastros == null || cadastros.isEmpty()) return true;
        boolean anyCpfDuplicado = verificaCpfDuplicado(cadastros);
        if(anyCpfDuplicado){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A solicitação não pode possuir mais de um cadastro com o mesmo CPF")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean verificaCpfDuplicado(List<CadastroRequestDTO> cadastros) {
        List<String> cpfs = cadastros.stream()
                .map(CadastroRequestDTO::cpf)
                .toList();
        return cpfs.stream().anyMatch(cpf -> Collections.frequency(cpfs, cpf) > 1);
    }
}