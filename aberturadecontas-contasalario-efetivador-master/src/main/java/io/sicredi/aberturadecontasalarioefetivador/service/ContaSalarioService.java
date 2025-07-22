package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.ContaSalarioResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalario;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalarioservice.ContaSalarioServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoCritica;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentEvent;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.sicredi.aberturadecontasalarioefetivador.factories.CriarContaSalarioFactory.toCriarContaSalario;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContaSalarioService {

    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String CANAL = "canal";
    private static final String CPF = "cpf";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_TYPE_CADASTRO = "CADASTRO";
    private static final String CODIGO_SUCESSO = "000";
    private static final String PREFIXO_CODIGO = "CCS";
    private static final String REGEX_SANITIZA_DESCRICAO = "^-\\s*";
    private final CadastroAssociadoContasService cadastroAssociadoContasService;
    private final ContaSalarioServiceClient contaSalarioServiceClient;
    private final CadastroRepository cadastroRepository;
    private final EnriquecimentoCadastroService enriquecimentoCadastroService;
    private final MetricService metricService;

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarSolicitacaoUnitaria(IdempotentAsyncRequest<Cadastro> cadastroRequest){
        Cadastro cadastro = cadastroRepository
                .findById(cadastroRequest.getValue().getId()).orElseThrow(NotFoundException::new);

        String transactionIdSolicitacao = cadastroRequest.getHeaders().get(TRANSACTION_ID_SOLICITACAO);
        String transactionId = cadastroRequest.getTransactionId();

        log.info("[{}] [{}] - Processando cadastro de Conta Salário. ", transactionIdSolicitacao, transactionId);
        Map<String, String> headers = new HashMap<>(cadastroRequest.getHeaders());
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);
        headers.put(CANAL, cadastro.getSolicitacao().getCanal());
        headers.put(CPF, cadastro.getCpf());
        headers.put(EVENT_TYPE, EVENT_TYPE_CADASTRO);

        boolean isAssociadoDigital = cadastroAssociadoContasService.isAssociadoDigital(cadastro.getCpf());

        CriarContaSalarioResponse criarContaSalarioResponse = criarContaSalario(cadastro, isAssociadoDigital);
        Cadastro cadastroAtualizado = atualizaCadastro(criarContaSalarioResponse.getContaSalarioResponse(), cadastro, isAssociadoDigital);

        cadastroRepository.save(cadastroAtualizado);

        log.info("[{}] [{}] - Processamento de Cadastro de Conta Salário finalizado.", transactionIdSolicitacao, transactionId);

        return IdempotentResponse
                .<Cadastro>builder()
                .value(cadastroAtualizado)
                .errorResponse(!cadastroAtualizado.isEfetivado())
                .headers(headers)
                .build();
    }

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarRespostaSolicitacaoUnitaria(IdempotentAsyncRequest<Cadastro> cadastroIdempotentAsyncRequest){
        String transactionIdSolicitacao = cadastroIdempotentAsyncRequest.getHeaders().get(TRANSACTION_ID_SOLICITACAO);
        String transactionId = cadastroIdempotentAsyncRequest.getHeaders().get(TRANSACTION_ID);
        Cadastro cadastro = cadastroIdempotentAsyncRequest.getValue();

        boolean isAssociadoDigital = cadastroAssociadoContasService.isAssociadoDigital(cadastro.getCpf());

        if(!isAssociadoDigital){
            try{
                enriquecimentoCadastroService.processarCadastro(cadastro,
                        transactionIdSolicitacao,
                        transactionId);
            }catch (Exception e){
                log.error("[{}] [{}] - Erro realizar enriquecimento de dados do cadastro. message: {}",
                        transactionIdSolicitacao,
                        transactionId, e.getMessage(), e);
            }
        }
        return processarRespostaSolicitacaoUnitaria(cadastro,
                cadastroIdempotentAsyncRequest.getTransactionId(), cadastroIdempotentAsyncRequest.getHeaders());
    }

    public IdempotentResponse<Cadastro> processarRespostaSolicitacaoUnitaria(Cadastro cadastroRequest, String transactionId, Map<String, String> headers){
        String transactionIdSolicitacao = headers.get(TRANSACTION_ID_SOLICITACAO);
        Cadastro cadastro = cadastroRepository.findById(cadastroRequest.getId())
                .orElseThrow(NotFoundException::new);

        log.info("[{}] [{}] - Processando resposta de Cadastro de Conta Salário.",
                transactionIdSolicitacao, transactionId);

        List<IdempotentEvent<?>> events = new ArrayList<>();

        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(TRANSACTION_ID, transactionId);
        newHeaders.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);
        newHeaders.put(CANAL, cadastro.getSolicitacao().getCanal());
        newHeaders.put(CPF, cadastro.getCpf());
        newHeaders.put(EVENT_TYPE, EVENT_TYPE_CADASTRO);

        log.info("[{}] [{}] - Processamento de resposta de Cadastro de Conta Salário finalizado.",
                transactionIdSolicitacao, transactionId);

        return IdempotentResponse
                .<Cadastro>builder()
                .value(cadastro)
                .events(events)
                .errorResponse(false)
                .headers(newHeaders)
                .build();
    }

    public CriarContaSalarioResponse criarContaSalario (Cadastro cadastro, boolean isPessoaDigital) {
        CriarContaSalario criarContaSalario = toCriarContaSalario(cadastro, isPessoaDigital);

        try {
            return contaSalarioServiceClient.criarContaSalario(criarContaSalario);
        } catch (Exception e){
            log.error("Erro ao acessar serviço ContaSalarioService para o cadastro {}", cadastro.getCpf(), e);
            throw new WebserviceException("Erro ao acessar serviço ContaSalarioService: " + e.getMessage(), e);
        }
    }

    private Cadastro atualizaCadastro(ContaSalarioResponse contaSalarioResponse, Cadastro cadastroOriginal, boolean isRelacionamentoDigital) {
        Cadastro cadastroAtualizado = cadastroOriginal.toBuilder().build();
        cadastroAtualizado.setProcessado(true);

        if(!CODIGO_SUCESSO.equals(contaSalarioResponse.getCodStatus())){
            cadastroAtualizado.setEfetivado(false);
            cadastroAtualizado.setSituacao(Resultado.ERRO);
            cadastroAtualizado.getCriticas().add(Critica.builder()
                    .tipo(TipoCritica.BLOQUEANTE)
                    .codigo(formataCodigoCritica(contaSalarioResponse.getCodStatus()))
                    .descricao(formataDescricaoCritica(contaSalarioResponse.getDesStatus())).build());
            metricService.incrementCounter("conta_salario_erro", "codigo", contaSalarioResponse.getCodStatus(), "descricao", contaSalarioResponse.getDesStatus());
        }
        else {
            cadastroAtualizado.setEfetivado(true);
            cadastroAtualizado.setConta(contaSalarioResponse.getNumConta());
            cadastroAtualizado.setSituacao(Resultado.CONCLUIDO);
        }

        if (isRelacionamentoDigital &&
                ((Objects.nonNull(cadastroOriginal.getDocumento())) || Objects.nonNull(cadastroOriginal.getEndereco())
                || Objects.nonNull(cadastroOriginal.getEmail()) || Objects.nonNull(cadastroOriginal.getTelefone()))) {
            cadastroAtualizado.getCriticas()
                    .add(Critica.builder()
                            .tipo(TipoCritica.INFORMATIVO)
                            .codigo("CCS018")
                            .descricao("Associado Digital - Dados cadastrais não atualizados.").build());
            metricService.incrementCounter("cadastro_associado_digital", "codigo", "CCS018");
        }

        return cadastroAtualizado;
    }

    private static String formataCodigoCritica(String codStatus) {
        return PREFIXO_CODIGO + codStatus;
    }
    private static String formataDescricaoCritica(String desStatus) {
        return desStatus.stripLeading()
                .replaceFirst(REGEX_SANITIZA_DESCRICAO, "")
                .toLowerCase()
                .transform(s -> s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1));
    }

}
