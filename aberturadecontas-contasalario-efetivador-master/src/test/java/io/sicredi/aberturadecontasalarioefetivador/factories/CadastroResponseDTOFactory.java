package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroResponseDTO;

public class CadastroResponseDTOFactory {
    public static CadastroResponseDTO cadastroMinimoEmProcessamento() {
        return cadastro(null);
    }

    public static CadastroResponseDTO cadastroCompletoEmProcessamento() {
        return cadastro("ASSOCIADO CONTA SALARIO 53744343227");
    }

    private static CadastroResponseDTO cadastro(String nome) {
        return CadastroResponseDTO.builder()
                .cpf("53744343227")
                .situacao("EM_PROCESSAMENTO")
                .nome(nome)
                .build();
    }
}
