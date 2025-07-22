package io.sicredi.aberturadecontasalarioefetivador.service;


import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;
import br.com.sicredi.contasalario.ejb.SaldoContaSalarioDTO;
import br.com.sicredi.mua.cada.business.server.ejb.ContaSalario;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociadoResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConsultarContaSalarioService {

    public static final String SIM = "SIM";
    public static final String NAO = "NAO";
    public static final String STATUS_CONTA_ATIVA = "ATIVA";
    public static final String STATUS_CONTA_ENCERRADA = "ENCERRADA";
    private final CadastroAssociadoService cadastroAssociadoService;
    private final CadastroAssociadoContasService cadastroAssociadoContasService;
    private final AberturaContaCoexistenciaService aberturaContaCoexistenciaService;
    private final ContaSalarioContasService contaSalarioContasService;
    private final ContaSalarioCoreEventosService contaSalarioCoreEventosService;
    private final MetricService metricService;


    public List<ConsultarContaSalarioResponseDTO> consultarContaSalario(String documento, String codigoConvenio, String canal) {

        if (Boolean.FALSE.equals("".equalsIgnoreCase(canal))) {
            metricService.incrementCounter("consultar_conta_salario",
                    "codigoConvenio", codigoConvenio,
                    "canal", canal);
        }

        try {
            ConsultarDadosAssociadoResponse dadosAssociadoResponse = cadastroAssociadoService.consultarDadosAssociado(documento);

            if (Objects.isNull(dadosAssociadoResponse.getOutConsultarDadosAssociado().getElementos())
                    || dadosAssociadoResponse.getOutConsultarDadosAssociado().getElementos().isEmpty()) {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.info("Consultar Conta Salário - Documento não encontrado: ", e);
            return Collections.emptyList();
        }

        List<CadastroAssociadoContasDTO> cadastroAssociadoContas = cadastroAssociadoContasService.buscarContasSalarioAssociado(documento);
        if (cadastroAssociadoContas.isEmpty()) {
            return Collections.emptyList();
        }

        return cadastroAssociadoContas.stream()
                .map(this::buscarContaSalario)
                .filter(contaSalarioCoop -> codigoConvenio.equalsIgnoreCase(contaSalarioCoop.getT2().getCodEmpresaConvenio()))
                .map(this::buscarContaSalarioContas)
                .map(ConsultarContaSalarioService::getConsultarContaSalarioResponseDTO)
                .toList();
    }

    private Tuple2<String, ContaSalario> buscarContaSalario(CadastroAssociadoContasDTO contaAssociado) {
        return Tuples.of(
                contaAssociado.cooperativa(),
                aberturaContaCoexistenciaService
                        .consultarContaSalario(
                                contaAssociado.conta(),
                                contaAssociado.cooperativa()
                        ).getReturn());
    }

    private static ConsultarContaSalarioResponseDTO getConsultarContaSalarioResponseDTO(Tuple3<ContaSalario,
                                                                                        SaldoContaSalarioDTO,
                                                                                        List<ConsultarContaSalarioResponseDTO.Alteracao>> it) {
        ContaSalario contaSalario = it.getT1();
        SaldoContaSalarioDTO saldoContaSalario = it.getT2();
        List<ConsultarContaSalarioResponseDTO.Alteracao> eventosAlteracaoContaSalario = it.getT3();

        return ConsultarContaSalarioResponseDTO.builder()
                .nome(contaSalario.getTitular())
                .documento(contaSalario.getCpf())
                .conta(contaSalario.getConta().replace("-",""))
                .agencia(saldoContaSalario.getNumeroAgenciaUsuario())
                .tipoContaSalario(
                        ContaSalarioEventoDestinoCredito.map(contaSalario.getDestinoCredCS()).getDescricao()
                )
                .dataAbertura(contaSalario.getDataAbertura().toString().split("T")[0])
                .dataEncerramento(
                        Objects.nonNull(contaSalario.getDataEncerramento())
                                ? contaSalario.getDataEncerramento().toString().split("T")[0] : null
                )
                .status(
                        Objects.isNull(contaSalario.getDataEncerramento())
                                ? STATUS_CONTA_ATIVA : STATUS_CONTA_ENCERRADA
                )
                .saldoAtual(
                        (Objects.nonNull(saldoContaSalario.getValorSaldoContaSalario())
                                && saldoContaSalario.getValorSaldoContaSalario().compareTo(BigDecimal.ZERO) > 0)
                                ? SIM : NAO
                )
                .saldoAnterior(
                        (Objects.nonNull(saldoContaSalario.getValorSaldoAnterior())
                                && saldoContaSalario.getValorSaldoAnterior().compareTo(BigDecimal.ZERO) > 0)
                                ? SIM : NAO
                )
                .portabilidade(
                        ConsultarContaSalarioResponseDTO.Portabilidade.builder()
                        .banco(contaSalario.getBancoDestino())
                        .agencia(contaSalario.getAgenciaDestino())
                        .conta(contaSalario.getContaDestino())
                        .tipoConta(processaTipoContaPortabilidade(contaSalario))
                        .build()
                )
                .convenio(
                        ConsultarContaSalarioResponseDTO.Convenio.builder()
                        .codigo(contaSalario.getCodEmpresaConvenio())
                        .cnpj(contaSalario.getCnpjEmpresaConvenio())
                        .nome(contaSalario.getNomeEmpresaConvenio())
                        .agencia(contaSalario.getAgenciaConvenio())
                        .conta(contaSalario.getContaConvenio())
                        .build()
                )
                .alteracoes(
                        eventosAlteracaoContaSalario.isEmpty() ? null : eventosAlteracaoContaSalario
                )
                .build();
    }

    private static String processaTipoContaPortabilidade(ContaSalario contaSalario) {
        if (Objects.isNull(contaSalario.getTipoContaDestino())
                || contaSalario.getTipoContaDestino().trim().isEmpty()) return null;
        return ContaSalarioEventoTipoConta.map(contaSalario.getTipoContaDestino()).getDescricao();
    }

    private Tuple3<ContaSalario, SaldoContaSalarioDTO, List<ConsultarContaSalarioResponseDTO.Alteracao>> buscarContaSalarioContas(Tuple2<String, ContaSalario> contaSalarioCoop) {
        String contaSalarioCooperativa = contaSalarioCoop.getT1();
        ContaSalario contaSalario = contaSalarioCoop.getT2();

        ConsultarSaldoContaSalarioResponse consultarSaldoContaSalarioResponse =
                contaSalarioContasService.consultarContaSalario(
                        contaSalarioCooperativa,
                        contaSalario.getConta().replace("-", "")
                );

        List<ConsultarContaSalarioResponseDTO.Alteracao> eventosAlteracaoContaSalario = new ArrayList<>();

        if (Objects.isNull(contaSalario.getDataEncerramento())) {
            eventosAlteracaoContaSalario =
                contaSalarioCoreEventosService.buscarEventosAlteracaoContaSalario(
                        contaSalarioCooperativa,
                        contaSalario.getConta().replace("-", "")
                );
        }

        if (Objects.isNull(consultarSaldoContaSalarioResponse.getOutConsultarSaldoContaSalario())
                || Objects.isNull(consultarSaldoContaSalarioResponse.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO())) {
            return Tuples.of(contaSalario, new SaldoContaSalarioDTO(), eventosAlteracaoContaSalario);
        }

        return Tuples.of(contaSalario,
                consultarSaldoContaSalarioResponse.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO(),
                eventosAlteracaoContaSalario);
    }
}
