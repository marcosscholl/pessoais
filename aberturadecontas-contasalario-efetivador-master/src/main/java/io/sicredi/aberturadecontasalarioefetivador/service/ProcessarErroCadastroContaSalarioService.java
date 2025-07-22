package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessarErroCadastroContaSalarioService {
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String CRITICA_CONTA_NO_CONVENIO = "ASSOCIADO JA POSSUI CONTA SALARIO PARA O CONVENIO INFORMADO";
    private static final String CRITICA_ERRO_GENERICO = "OSB SERVICE CALLOUT ACTION RECEIVED SOAP FAULT RESPONSE";
    private static final String CPF = "cpf";
    private static final String CANAL = "canal";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_TYPE_CADASTRO = "CADASTRO";
    private final CadastroRepository cadastroRepository;
    private final GestentService gestentService;
    private final SolicitacaoRepository solicitacaoRepository;
    private final CadastroAssociadoService cadastroAssociadoService;
    private final CadastroAssociadoContasService cadastroAssociadoContasService;
    private final AberturaContaCoexistenciaService aberturaContaCoexistenciaService;
    private final ContaSalarioService contaSalarioService;
    private final MetricService metricService;

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarErroCadastroContaSalario(IdempotentAsyncRequest<Cadastro> cadastroErroIdempotentAsyncRequest) {

        String transactionIdSolicitacao = cadastroErroIdempotentAsyncRequest.getHeaders().get(TRANSACTION_ID_SOLICITACAO);
        String transactionId = cadastroErroIdempotentAsyncRequest.getTransactionId();
        Map<String, String> headers = new HashMap<>(cadastroErroIdempotentAsyncRequest.getHeaders());
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);
        headers.put(CPF, cadastroErroIdempotentAsyncRequest.getValue().getCpf());
        headers.put(EVENT_TYPE, EVENT_TYPE_CADASTRO);

        log.info("[{}] [{}] - Processando erro de cadastro de Conta Salário.", transactionIdSolicitacao, transactionId);

        if (Objects.nonNull(cadastroErroIdempotentAsyncRequest.getValue().getCriticas())
                && cadastroErroIdempotentAsyncRequest.getValue()
                .getCriticas()
                .stream()
                .anyMatch(critica -> critica.getDescricao().toUpperCase()
                        .contains(CRITICA_CONTA_NO_CONVENIO) || critica.getDescricao().toUpperCase().contains(CRITICA_ERRO_GENERICO))) {
            log.info("[{}] [{}] - Processando erro de cadastro de Conta Salário. Tratamento específico.", transactionIdSolicitacao, transactionId);
            Cadastro cadastro = cadastroRepository
                    .findById(cadastroErroIdempotentAsyncRequest.getValue().getId()).orElseThrow(NotFoundException::new);

            Cadastro cadastroAtualizado = processarContaSalarioExistente(cadastro);

            log.info("[{}] [{}] - Processamento erro de Cadastro de Conta Salário finalizado. Tratamento específico.", transactionIdSolicitacao, transactionId);
            headers.put(CANAL, cadastroAtualizado.getSolicitacao().getCanal());
            return contaSalarioService.processarRespostaSolicitacaoUnitaria(cadastroAtualizado, transactionId, headers);

        }
        var cadastro = cadastroErroIdempotentAsyncRequest.getValue();
        if (!cadastro.isEfetivado() && !Resultado.ERRO.equals(cadastro.getSituacao())) {
            log.info("[{}] [{}] - Processando erro de cadastro de Conta Salário não efetivado e sem erro.", transactionIdSolicitacao, transactionId);
            Cadastro cadastroAtualizado = cadastroRepository
                    .findById(cadastro.getId()).orElseThrow(NotFoundException::new);
            cadastroAtualizado.setEfetivado(false);
            cadastroAtualizado.setSituacao(Resultado.ERRO);
            cadastro = cadastroRepository.save(cadastroAtualizado);
            headers.put(CANAL, cadastroAtualizado.getSolicitacao().getCanal());

            log.info("[{}] [{}] - Processamento erro de cadastro de Conta Salário não efetivado e sem erro finalizado.", transactionIdSolicitacao, transactionId);

        }

        log.info("[{}] [{}] - Processamento erro de Cadastro de Conta Salário finalizado.", transactionIdSolicitacao, transactionId);

        return contaSalarioService.processarRespostaSolicitacaoUnitaria(cadastro, transactionId, headers);

    }

    private Cadastro processarContaSalarioExistente(Cadastro cadastro) {
        if (Objects.isNull(cadastro.getSolicitacao().getBranchCode())) {
            log.info("[{}] - Buscando entidade branchCode para a solicitação pelo cadastro.", cadastro.getCpf());
            Optional<String> branchCode = gestentService.consultarCodigoEntidade(cadastro.getSolicitacao().getNumCooperativa(),
                    cadastro.getSolicitacao().getNumAgencia());
            if (branchCode.isEmpty()) {
                return cadastro;
            }
            cadastro.getSolicitacao().setBranchCode(branchCode.get());
            solicitacaoRepository.save(cadastro.getSolicitacao());
            log.info("[{}] - Atualizada entidade branchCode para a solicitação pelo cadastro.", cadastro.getCpf());
        }

        List<CadastroAssociadoContasDTO> cadastroContas = cadastroAssociadoContasService.buscarContasAssociado(cadastro.getCpf(), cadastro.getSolicitacao().getNumCooperativa());

        if (cadastroContas.isEmpty()) {
            return cadastro;
        }

        if (Objects.isNull(cadastro.getOidPessoa())) {
            Optional<Long> oidPessoa = cadastroAssociadoService.consultarCadastroOidPessoa(cadastro);
            if (oidPessoa.isEmpty()) {
                return cadastro;
            }
            cadastro.setOidPessoa(oidPessoa.get());
        }

        try {
            GetContaSalarioResponse getContaSalarioResponse = aberturaContaCoexistenciaService.consultarContaSalario(cadastro.getSolicitacao().getBranchCode(),
                    cadastroContas.getFirst().conta(),
                    cadastro.getSolicitacao().getNumCooperativa(),
                    cadastro.getCpf(),
                    cadastro.getOidPessoa());

            if (Objects.equals(getContaSalarioResponse.getReturn().getCodEmpresaConvenio(), cadastro.getSolicitacao().getCodConvenioFontePagadora())) {
                Set<Critica> criticas = cadastro.getCriticas()
                        .stream()
                        .filter(critica -> !critica.getDescricao()
                                .toUpperCase()
                                .contains(CRITICA_CONTA_NO_CONVENIO))
                        .filter(critica -> !critica.getDescricao()
                                .toUpperCase()
                                .contains(CRITICA_ERRO_GENERICO))
                        .collect(Collectors.toSet());

                cadastro.setCriticas(criticas);
                cadastro.setEfetivado(true);
                cadastro.setConta(getContaSalarioResponse.getReturn().getConta().replace("-", ""));
                cadastro.setSituacao(Resultado.CONCLUIDO);
                metricService.incrementCounter("erroCadastro_conta_pre_existente_reaproveitada");
            }
            return cadastroRepository.save(cadastro);
        } catch (Exception e) {
            log.error("Erro ao processar conta salario existente do CPF {} : {}", cadastro.getCpf(), e.getMessage());
            return cadastro;
        }
    }
}
