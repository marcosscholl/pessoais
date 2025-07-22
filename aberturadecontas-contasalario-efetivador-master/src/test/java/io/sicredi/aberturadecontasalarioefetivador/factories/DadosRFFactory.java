package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.entities.DadosRF;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DadosRFFactory {


    public static DadosRF maiorDeIdadeRegular() {
        return DadosRF.builder()
                .id(1L)
                .anoObito(null)
                .codigoSituacaoCadastral("0")
                .dataNascimento(LocalDate.parse("08/05/1978", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .descSituacaoCadastral("Quando não há nenhuma pendência no cadastro do contribuinte.")
                .nome("Cadastro Conta Salario")
                .sexo("Masculino")
                .situacaoCadastral("Regular")
                .build();
    }

    public static DadosRF maiorDeIdadeIrregular() {
        return maiorDeIdadeRegular().toBuilder()
                .situacaoCadastral("Cancelada de Ofício")
                .descSituacaoCadastral("Cancelada de Ofício")
                .build();
    }

    public static DadosRF menorDeIdadeRegular() {
        return maiorDeIdadeRegular().toBuilder()
                .dataNascimento(LocalDate.now().minusYears(10)).build();
    }
}
