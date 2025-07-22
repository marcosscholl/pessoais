package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;

import java.util.List;

public class ConsultarContaSalarioResponseDTOFactory {

    public static final String NOME = "VALOR DEFAULT MOCK";
    public static final String CONTA_SALARIO = "903677";
    public static final String AGENCIA_CONTA_SALARIO = "0167";
    public static final String TIPO_CONTA_SALARIO = "PORTABILIDADE_OUTRA_INSTITUICAO";
    public static final String SIM = "SIM";
    public static final String NAO = "NAO";
    public static final String CODIGO_BANCO_C6 = "336";
    public static final String TIPO_ALTERACAO_PORTABILIDADE = "ALTERACAO_PORTABILIDADE";

    public static List<ConsultarContaSalarioResponseDTO> consultarContaSalarioResponseDTO() {
        ConsultarContaSalarioResponseDTO dto = ConsultarContaSalarioResponseDTO.builder()
                .nome(NOME)
                .documento("36185900084")
                .conta(CONTA_SALARIO)
                .agencia(AGENCIA_CONTA_SALARIO)
                .dataAbertura("2025-03-19")
                .tipoContaSalario(TIPO_CONTA_SALARIO)
                .saldoAtual(SIM)
                .saldoAnterior(NAO)
                .convenio(
                        ConsultarContaSalarioResponseDTO.Convenio.builder()
                                .codigo("3AO")
                                .cnpj("18523110000101")
                                .nome("ELLETRISA ENGENHARI")
                                .agencia("0810")
                                .conta("801606")
                                .build()
                )
                .portabilidade(
                        ConsultarContaSalarioResponseDTO.Portabilidade.builder()
                                .banco(CODIGO_BANCO_C6)
                                .agencia("1590")
                                .conta("502553")
                                .tipoConta("CONTA_CORRENTE_INDIVIDUAL")
                                .build()
                )
                .alteracoes(List.of(
                        ConsultarContaSalarioResponseDTO.Alteracao.builder()
                                .tipo(TIPO_ALTERACAO_PORTABILIDADE)
                                .dataAtualizacao("2025-03-19T13:39:08.935")
                                .dadosAlterados(List.of(
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("CODIGO_BANCO_DESTINO")
                                                .anterior("001")
                                                .atual(CODIGO_BANCO_C6)
                                                .build()
                                ))
                                .build(),
                        ConsultarContaSalarioResponseDTO.Alteracao.builder()
                                .tipo(TIPO_ALTERACAO_PORTABILIDADE)
                                .dataAtualizacao("2025-03-24T18:34:00.915")
                                .dadosAlterados(List.of(
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("DESTINO_CREDITO_SALARIO")
                                                .anterior(TIPO_CONTA_SALARIO)
                                                .atual("MODALIDADE_SAQUE")
                                                .build()
                                ))
                                .build(),
                        ConsultarContaSalarioResponseDTO.Alteracao.builder()
                                .tipo(TIPO_ALTERACAO_PORTABILIDADE)
                                .dataAtualizacao("2025-03-24T18:34:59.08")
                                .dadosAlterados(List.of(
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("NUMERO_AGENCIA_DESTINO")
                                                .anterior("")
                                                .atual("1590")
                                                .build(),
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("CODIGO_BANCO_DESTINO")
                                                .anterior("")
                                                .atual(CODIGO_BANCO_C6)
                                                .build(),
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("DESTINO_CREDITO_SALARIO")
                                                .anterior("MODALIDADE_SAQUE")
                                                .atual(TIPO_CONTA_SALARIO)
                                                .build(),
                                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                                .nome("NUMERO_CONTA_DESTINO")
                                                .anterior("")
                                                .atual("502553")
                                                .build()
                                ))
                                .build()
                ))
                .build();

        return List.of(dto);
    }
}
