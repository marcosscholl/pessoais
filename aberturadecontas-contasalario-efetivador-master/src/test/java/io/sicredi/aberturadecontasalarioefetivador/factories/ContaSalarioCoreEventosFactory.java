package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioCoreEventosDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioEventoDestinoCredito;

import java.util.List;

public class ContaSalarioCoreEventosFactory {

    public static final String AGENCIA = "0167";
    public static final String CONTA_SALARIO = "903677";
    public static final String EVENTO_CRIACAO_CONTA_SALARIO = "CRIACAO_CONTA_SALARIO";
    public static final String CAMPO_NUM_AGENCIA_USUARIO = "NUM_AGENCIA_USUARIO";
    public static final String CAMPO_NUM_BANCO = "NUM_BANCO";
    public static final String CODIGO_BANCO_BB = "001";
    public static final String EVENTO_ALTERACAO_PORTABILIDADE = "ALTERACAO_PORTABILIDADE";
    public static final String CODIGO_BANCO_C6 = "336";
    public static final String EVENTO_ALTERACAO_SALDO = "ALTERACAO_SALDO";
    public static final String CAMPO_VLR_SALDO_CS = "VLR_SALDO_CS";
    public static final String SALDO_ZERO = "0";
    public static final String SALDO_ATUALIZADO = "1000.00";
    public static final String CAMPO_COD_DESTINO_CRED_CS = "COD_DESTINO_CRED_CS";
    public static final String CAMPO_PORTABILIDADE_IF = "C";
    public static final String CAMPO_MODALIDADE_SAQUE = "A";
    public static final String CAMPO_NUM_AGENCIA_PORTABILIDADE = "NUM_AGENCIA";
    public static final String AGENCIA_PORTABILIDADE = "1590";
    public static final String CAMPO_NUM_CONTA_DESTINO = "NUM_CONTA_DESTINO";
    public static final String CAMPO_CONTA_PORTABILIDADE = "502553";
    public static final String CAMPO_CODIGO_BANCO_DESTINO = "CODIGO_BANCO_DESTINO";
    public static final String CAMPO_DESTINO_CREDITO_SALARIO = "DESTINO_CREDITO_SALARIO";
    public static final String CAMPO_NUMERO_AGENCIA_DESTINO = "NUMERO_AGENCIA_DESTINO";
    public static final String CAMPO_NUMERO_CONTA_DESTINO = "NUMERO_CONTA_DESTINO";
    public static final String CAMPO_NUM_EMPRESA_CONVENIADA = "NUM_EMPRESA_CONVENIADA";
    public static final String CODIGO_CONVENIO = "3AO";
    public static final String EVENTO_ALTERACAO_CONVENIO = "ALTERACAO_CONVENIO";
    public static final String EVENTO_ENCERRAMENTO_CONTA_SALARIO = "ENCERRAMENTO_CONTA_SALARIO";
    public static final String CAMPO_CODIGO_CONVENIO = "CODIGO_CONVENIO";
    public static final String CAMPO_MOTIVO_ALTERACAO = "MOTIVO_ALTERACAO";
    public static final String CAMPO_DES_MOTIVO_ENCERRAMENTO = "DES_MOTIVO_ENCERRAMENTO";
    public static final String JUSTIFICATIVA_MOTIVO_ALTERACAO = "Tentativas de portabilidade excedida, conta salario alterada para saque.";

    public static List<ContaSalarioCoreEventosDTO> contaSalarioCoreEventos() {
        ContaSalarioCoreEventosDTO evento1 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_CRIACAO_CONTA_SALARIO))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_AGENCIA_USUARIO)
                                .antes(null)
                                .depois(AGENCIA)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_BANCO)
                                .antes(null)
                                .depois(CODIGO_BANCO_BB)
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento2 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ALTERACAO_PORTABILIDADE))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_BANCO)
                                .antes(CODIGO_BANCO_BB)
                                .depois(CODIGO_BANCO_C6)
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento3 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ALTERACAO_SALDO))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_VLR_SALDO_CS)
                                .antes(SALDO_ZERO)
                                .depois(SALDO_ATUALIZADO)
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento4 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ALTERACAO_PORTABILIDADE))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_COD_DESTINO_CRED_CS)
                                .antes(CAMPO_PORTABILIDADE_IF)
                                .depois(CAMPO_MODALIDADE_SAQUE)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_AGENCIA_PORTABILIDADE)
                                .antes(AGENCIA_PORTABILIDADE)
                                .depois(null)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome("COD_TIPO_CONTA")
                                .antes("02")
                                .depois(" ")
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento5 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ALTERACAO_PORTABILIDADE))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_AGENCIA_PORTABILIDADE)
                                .antes(null)
                                .depois(AGENCIA_PORTABILIDADE)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_BANCO)
                                .antes(null)
                                .depois(CODIGO_BANCO_C6)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_COD_DESTINO_CRED_CS)
                                .antes(CAMPO_MODALIDADE_SAQUE)
                                .depois(CAMPO_PORTABILIDADE_IF)
                                .build(),
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_CONTA_DESTINO)
                                .antes(null)
                                .depois(CAMPO_CONTA_PORTABILIDADE)
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento6 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ALTERACAO_CONVENIO))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_NUM_EMPRESA_CONVENIADA)
                                .antes(CODIGO_CONVENIO)
                                .depois("3B0")
                                .build()
                ))
                .build();

        ContaSalarioCoreEventosDTO evento7 = ContaSalarioCoreEventosDTO.builder()
                .agencia(AGENCIA)
                .conta(CONTA_SALARIO)
                .tipos(List.of(EVENTO_ENCERRAMENTO_CONTA_SALARIO))
                .camposAlterados(List.of(
                        ContaSalarioCoreEventosDTO.CamposAlteradosDTO.builder()
                                .nome(CAMPO_DES_MOTIVO_ENCERRAMENTO)
                                .antes(null)
                                .depois(JUSTIFICATIVA_MOTIVO_ALTERACAO)
                                .build()
                ))
                .build();

        return List.of(evento1, evento2, evento3, evento4, evento5, evento6, evento7);
    }

    public static List<ConsultarContaSalarioResponseDTO.Alteracao> contaSalarioEventosAlteracao() {

        ConsultarContaSalarioResponseDTO.Alteracao evento1 = ConsultarContaSalarioResponseDTO.Alteracao.builder()
                .tipo(EVENTO_ALTERACAO_PORTABILIDADE)
                .dadosAlterados(List.of(
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_CODIGO_BANCO_DESTINO)
                                .anterior(CODIGO_BANCO_BB)
                                .atual(CODIGO_BANCO_C6)
                                .build()
                ))
                .build();

        ConsultarContaSalarioResponseDTO.Alteracao evento2 = ConsultarContaSalarioResponseDTO.Alteracao.builder()
                .tipo(EVENTO_ALTERACAO_PORTABILIDADE)
                .dadosAlterados(List.of(
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_DESTINO_CREDITO_SALARIO)
                                .anterior(ContaSalarioEventoDestinoCredito.map(CAMPO_PORTABILIDADE_IF).getDescricao())
                                .atual(ContaSalarioEventoDestinoCredito.map(CAMPO_MODALIDADE_SAQUE).getDescricao())
                                .build(),
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_NUMERO_AGENCIA_DESTINO)
                                .anterior(AGENCIA_PORTABILIDADE)
                                .atual("")
                                .build(),
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome("TIPO_CONTA_SALARIO")
                                .anterior("CONTA_POUPANCA_INDIVIDUAL")
                                .atual("")
                                .build()
                ))
                .build();

        ConsultarContaSalarioResponseDTO.Alteracao evento3 = ConsultarContaSalarioResponseDTO.Alteracao.builder()
                .tipo(EVENTO_ALTERACAO_PORTABILIDADE)
                .dadosAlterados(List.of(
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_NUMERO_AGENCIA_DESTINO)
                                .anterior("")
                                .atual(AGENCIA_PORTABILIDADE)
                                .build(),
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_CODIGO_BANCO_DESTINO)
                                .anterior("")
                                .atual(CODIGO_BANCO_C6)
                                .build(),
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_DESTINO_CREDITO_SALARIO)
                                .anterior(ContaSalarioEventoDestinoCredito.map(CAMPO_MODALIDADE_SAQUE).getDescricao())
                                .atual(ContaSalarioEventoDestinoCredito.map(CAMPO_PORTABILIDADE_IF).getDescricao())
                                .build(),
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_NUMERO_CONTA_DESTINO)
                                .anterior("")
                                .atual(CAMPO_CONTA_PORTABILIDADE)
                                .build()
                ))
                .build();

        ConsultarContaSalarioResponseDTO.Alteracao evento4 = ConsultarContaSalarioResponseDTO.Alteracao.builder()
                .tipo(EVENTO_ALTERACAO_CONVENIO)
                .dadosAlterados(List.of(
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_CODIGO_CONVENIO)
                                .anterior(CODIGO_CONVENIO)
                                .atual("3B0")
                                .build()
                ))
                .build();

        ConsultarContaSalarioResponseDTO.Alteracao evento5 = ConsultarContaSalarioResponseDTO.Alteracao.builder()
                .tipo(EVENTO_ENCERRAMENTO_CONTA_SALARIO)
                .dadosAlterados(List.of(
                        ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                .nome(CAMPO_MOTIVO_ALTERACAO)
                                .anterior("")
                                .atual(JUSTIFICATIVA_MOTIVO_ALTERACAO)
                                .build()
                ))
                .build();

        return List.of(evento1, evento2, evento3, evento4, evento5);
    }

}
