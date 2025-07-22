package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;

import java.time.LocalDate;

public class BureauRFDTOFactory {
    public static BureauRFDTO maiorDeIdadeRegular() {
        return BureauRFDTO.builder()
                .codigoSituacaoCadastral("0")
                .dataNascimento(LocalDate.of(1978, 5, 8))
                .dataAtualizacao("20/09/2024")
                .descSituacaoCadastral("Quando não há nenhuma pendência no cadastro do contribuinte.")
                .nome("Cadastro Conta Salario")
                .nomeMae("Mãe Cadastro Conta Salario")
                .residenteExterior("Não Residente")
                .sexo("Masculino")
                .situacaoCadastral("REGULAR")
                .build();
    }

    public static BureauRFDTO maiorDeIdadeIrregular() {
        return maiorDeIdadeRegular().toBuilder()
                .situacaoCadastral("Cancelada de Ofício")
                .descSituacaoCadastral("Cancelada de Ofício")
                .build();
    }

    public static BureauRFDTO menorDeIdadeRegular() {
        return maiorDeIdadeRegular().toBuilder()
                .dataNascimento(LocalDate.now().minusYears(10)).build();
    }
}
