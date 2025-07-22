package io.sicredi.aberturadecontasalarioefetivador.dto;

import java.util.List;

public record CadastroAssociadoContasDTO(
    String cooperativa,
    String posto,
    String conta,
    String dataAbertura,
    String dataEncerramento,
    String clienteDesde,
    Boolean identificacaoDepositante,
    String originacao,
    String tipoPessoa,
    String tipoAplicador,
    StatusRelacionamento statusRelacionamento,
    List<Tipo> tipos,
    List<Relacionamento> relacionamentos
){
    public record StatusRelacionamento(
            String status
    ) {}

    public record Tipo(
            String tipo,
            String status
    ) {}

    public record Relacionamento(
            String tipoRelacionamento,
            String nome,
            String documento,
            String dataNascimentoConstituicao,
            String originacao
    ) {}
}
