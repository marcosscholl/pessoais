package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocoreeventos.ContaSalarioCoreEventosClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioCoreEventosDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioEventoDestinoCredito;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioEventoTipoConta;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class ContaSalarioCoreEventosService {

    private static final String EVENTO_ALTERACAO_PORTABILIDADE = "ALTERACAO_PORTABILIDADE";
    private static final String EVENTO_ALTERACAO_CONVENIO = "ALTERACAO_CONVENIO";
    private static final String EVENTO_ENCERRAMENTO_CONTA_SALARIO = "ENCERRAMENTO_CONTA_SALARIO";
    private static final String CAMPO_ALTERACAO_COD_DESTINO_CRED_CS = "COD_DESTINO_CRED_CS";
    private static final String NOVO_CAMPO_ALTERACAO_COD_DESTINO_CRED_CS = "DESTINO_CREDITO_SALARIO";
    private static final String CAMPO_ALTERACAO_COD_TIPO_CONTA = "COD_TIPO_CONTA";
    private static final String NOVO_CAMPO_ALTERACAO_COD_TIPO_CONTA = "TIPO_CONTA_SALARIO";
    private static final String CAMPO_ALTERACAO_NUM_AGENCIA = "NUM_AGENCIA";
    private static final String NOVO_CAMPO_ALTERACAO_NUM_AGENCIA = "NUMERO_AGENCIA_DESTINO";
    private static final String CAMPO_ALTERACAO_NUM_BANCO = "NUM_BANCO";
    private static final String NOVO_CAMPO_ALTERACAO_NUM_BANCO = "CODIGO_BANCO_DESTINO";
    private static final String CAMPO_ALTERACAO_NUM_CONTA_DESTINO = "NUM_CONTA_DESTINO";
    private static final String NOVO_CAMPO_ALTERACAO_NUM_CONTA_DESTINO = "NUMERO_CONTA_DESTINO";
    private static final String CAMPO_ALTERACAO_DES_MOTIVO_ENCERRAMENTO = "DES_MOTIVO_ENCERRAMENTO";
    private static final String NOVO_CAMPO_ALTERACAO_DES_MOTIVO_ENCERRAMENTO = "MOTIVO_ALTERACAO";
    public static final String CAMPO_ALTERACAO_NUM_EMPRESA_CONVENIADA = "NUM_EMPRESA_CONVENIADA";
    public static final String NOVO_CAMPO_ALTERACAO_CODIGO_CONVENIO = "CODIGO_CONVENIO";
    private final ContaSalarioCoreEventosClient client;

    public List<ContaSalarioCoreEventosDTO> buscarEventosContaSalario(String agencia, String conta) {
        try {
            return client.buscarEventosContaSalario(agencia, conta);
        } catch (Exception e) {
            log.debug("Erro ao consultar eventos de conta salário: ", e);
            return List.of();
        }
    }

    public List<ConsultarContaSalarioResponseDTO.Alteracao> buscarEventosAlteracaoContaSalario(String agencia, String conta) {
        List<ContaSalarioCoreEventosDTO> eventos = buscarEventosContaSalario(agencia, conta);
        return processarEventosAlteracoes(eventos);
    }

    private static List<ConsultarContaSalarioResponseDTO.Alteracao> processarEventosAlteracoes(List<ContaSalarioCoreEventosDTO> contaSalarioCoreEventos) {
        List<ConsultarContaSalarioResponseDTO.Alteracao> alteracoes = new ArrayList<>();
        contaSalarioCoreEventos.forEach(evento -> {
            String tipoEvento = evento.tipos().isEmpty() ? "" : evento.tipos().getFirst();

            if (EVENTO_ALTERACAO_PORTABILIDADE.equalsIgnoreCase(tipoEvento)
                    || EVENTO_ALTERACAO_CONVENIO.equalsIgnoreCase(tipoEvento)
                    || EVENTO_ENCERRAMENTO_CONTA_SALARIO.equalsIgnoreCase(tipoEvento)) {
                montarAlteracao(evento, alteracoes);
            }
        });
        return alteracoes;
    }

    private static void montarAlteracao(
            ContaSalarioCoreEventosDTO evento,
            Collection<ConsultarContaSalarioResponseDTO.Alteracao> alteracoes
    ) {
        if (evento.camposAlterados().isEmpty()) {
            return;
        }
        List<ConsultarContaSalarioResponseDTO.DadosAlterados> dadosAlterados = new ArrayList<>();
        evento.camposAlterados().stream()
                .forEach(campo -> {
                    if (CAMPO_ALTERACAO_COD_DESTINO_CRED_CS.equalsIgnoreCase(campo.nome())) {
                        dadosAlterados.add(
                                ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                        .nome(processarNomeCampoAlteracao(campo.nome()))
                                        .anterior(ContaSalarioEventoDestinoCredito.map(campo.antes()).getDescricao())
                                        .atual(ContaSalarioEventoDestinoCredito.map(campo.depois()).getDescricao())
                                        .build()
                        );
                    } else if (CAMPO_ALTERACAO_COD_TIPO_CONTA.equalsIgnoreCase(campo.nome())) {
                        dadosAlterados.add(
                                ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                        .nome(processarNomeCampoAlteracao(campo.nome()))
                                        .anterior(processaTipoConta(campo.antes()))
                                        .atual(processaTipoConta(campo.depois()))
                                        .build()
                        );
                    } else {
                        dadosAlterados.add(
                                ConsultarContaSalarioResponseDTO.DadosAlterados.builder()
                                        .nome(processarNomeCampoAlteracao(campo.nome()))
                                        .anterior(Objects.isNull(campo.antes()) ? "" : campo.antes())
                                        .atual(Objects.isNull(campo.depois()) ? "" : campo.depois())
                                        .build()
                        );
                    }
                });

        alteracoes.add(
                ConsultarContaSalarioResponseDTO.Alteracao.builder()
                        .tipo(evento.tipos().getFirst())
                        .dataAtualizacao(evento.dataCriacao())
                        .dadosAlterados(dadosAlterados)
                        .build()
        );
    }

    private static String processaTipoConta(String campo) {
        if (Objects.isNull(campo)
                || campo.trim().isEmpty()) return "";
        return ContaSalarioEventoTipoConta.map(campo).getDescricao();
    }

    private static String processarNomeCampoAlteracao(String nomeCampo) {
        return switch (nomeCampo) {
            case CAMPO_ALTERACAO_COD_DESTINO_CRED_CS -> NOVO_CAMPO_ALTERACAO_COD_DESTINO_CRED_CS;
            case CAMPO_ALTERACAO_COD_TIPO_CONTA -> NOVO_CAMPO_ALTERACAO_COD_TIPO_CONTA;
            case CAMPO_ALTERACAO_NUM_AGENCIA -> NOVO_CAMPO_ALTERACAO_NUM_AGENCIA;
            case CAMPO_ALTERACAO_NUM_BANCO -> NOVO_CAMPO_ALTERACAO_NUM_BANCO;
            case CAMPO_ALTERACAO_NUM_CONTA_DESTINO -> NOVO_CAMPO_ALTERACAO_NUM_CONTA_DESTINO;
            case CAMPO_ALTERACAO_DES_MOTIVO_ENCERRAMENTO -> NOVO_CAMPO_ALTERACAO_DES_MOTIVO_ENCERRAMENTO;
            case CAMPO_ALTERACAO_NUM_EMPRESA_CONVENIADA -> NOVO_CAMPO_ALTERACAO_CODIGO_CONVENIO;
            default -> nomeCampo;
        };
    }
}
