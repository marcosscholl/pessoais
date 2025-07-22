package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Status;
import io.sicredi.aberturadecontasalarioefetivador.mapper.SolicitacaoMapper;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String RESULTADO = "resultado";
    private static final String CANAL = "canal";
    private static final String STATUS = "status";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_TYPE_SOLICITACAO = "SOLICITACAO";
    public static final String CABECALHO_TRANSACTION_ID = "transactionId";
    public static final String TOPICO_PROCESSAMENTO_BUREAU_RF = "aberturadecontas-contasalario-efetivador-bureaurf-v1";
    public static final String CODIGO_SUFIXO_PROCESSO_REQUISITADO = "1";
    private final SolicitacaoRepository solicitacaoRepository;
    private final SolicitacaoMapper solicitacaoMapper;
    private final GestentService gestentService;
    private final ContaSalarioService contaSalarioService;
    private final MetricService metricService;
    private final CadastroRepository cadastroRepository;
    private final WebhookService webhookService;

    public SolicitacaoResponseDTO consultarSolicitacao(BigInteger idTransacao) {
        var solicitacaoOptional = consultarSolicitacaoCompleta(idTransacao);
        if (solicitacaoOptional.isPresent()) {
            return solicitacaoMapper.map(solicitacaoOptional.get());
        }
        return SolicitacaoResponseDTO.builder().build();
    }

    public Optional<Solicitacao> consultarSolicitacaoCompleta(BigInteger idTransacao) {
        return solicitacaoRepository.findByIdTransacao(idTransacao);
    }

    @IdempotentTransaction
    public IdempotentResponse<SolicitacaoResponseDTO> processarSolicitacao(IdempotentRequest<SolicitacaoRequestDTO> request,
                                                                           String transactionIdSolicitacao,
                                                                           String canal,
                                                                           String autorizacaoRetorno) {
        var solicitacaoCadastroContaSalarioDTO = request.getValue();
        try {
            log.info("[{}] - Salvando nova solicitação de Cadastros.", transactionIdSolicitacao);
            var solicitacaoCadastroContaSalario = criarNovaSolicitacao(solicitacaoCadastroContaSalarioDTO, transactionIdSolicitacao, canal, autorizacaoRetorno);
            solicitacaoCadastroContaSalario.setIdTransacao(new BigInteger(transactionIdSolicitacao));
            List<IdempotentEvent<?>> eventos = criarEventosDeCadastro(request, solicitacaoCadastroContaSalario);

            var responseDTO = solicitacaoMapper.map(solicitacaoCadastroContaSalario);

            log.info("[{}] - Nova solicitacao de Cadastros salva com sucesso. ", solicitacaoCadastroContaSalario.getIdTransacao());

            metricService.incrementCounter("solicitacao_sucesso",
                    "codConvenioFontePagadora", solicitacaoCadastroContaSalarioDTO.codConvenioFontePagadora(),
                    "numCooperativa", solicitacaoCadastroContaSalarioDTO.numCooperativa(),
                    "agencia", solicitacaoCadastroContaSalarioDTO.numAgencia(),
                    CANAL, canal);

            return IdempotentResponse
                    .<SolicitacaoResponseDTO>builder()
                    .value(responseDTO)
                    .errorResponse(false)
                    .events(eventos)
                    .headers(request.getHeaders())
                    .build();

        } catch (Exception e) {
            metricService.incrementCounter("solicitacao_erro",
                            "codConvenioFontePagadora", solicitacaoCadastroContaSalarioDTO.codConvenioFontePagadora(),
                            "numCooperativa", solicitacaoCadastroContaSalarioDTO.numCooperativa(),
                            "agencia", solicitacaoCadastroContaSalarioDTO.numAgencia(),
                            CANAL, canal,
                            "errorType", e.getClass().getSimpleName());
            log.error("[{}] - Erro ao salvar nova solicitação de Cadastros. {}", request.getTransactionId(), request.getValue(), e);
            throw e;
        }
    }

    private Solicitacao criarNovaSolicitacao(SolicitacaoRequestDTO solicitacaoDTO,
                                             String transactionId,
                                             String canal,
                                             String autorizacaoRetorno) {

        var solicitacaoCadastroContaSalario = inicializaSolicitacao(solicitacaoDTO, transactionId, canal, autorizacaoRetorno);

        String branchCode = gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(solicitacaoCadastroContaSalario.getNumCooperativa(),
                solicitacaoCadastroContaSalario.getNumAgencia());
        solicitacaoCadastroContaSalario.setBranchCode(branchCode);

        return solicitacaoRepository.save(solicitacaoCadastroContaSalario);
    }

    private List<IdempotentEvent<?>> criarEventosDeCadastro(IdempotentRequest<SolicitacaoRequestDTO> request,
                                                            Solicitacao solicitacao) {
        List<IdempotentEvent<?>> eventos = new ArrayList<>();

        solicitacao.getCadastros().forEach(cadastro -> {
            Map<String, String> headers = new HashMap<>(request.getHeaders());
            headers.put(CABECALHO_TRANSACTION_ID, request.getTransactionId()
                    .concat(cadastro.getId().toString())
                    .concat(CODIGO_SUFIXO_PROCESSO_REQUISITADO));

            eventos.add(IdempotentEvent.<Cadastro>builder()
                    .value(cadastro)
                    .headers(headers)
                    .topic(TOPICO_PROCESSAMENTO_BUREAU_RF)
                    .build());
        });
        return eventos;
    }

    private Solicitacao inicializaSolicitacao(SolicitacaoRequestDTO solicitacaoDTO,
                                              String transactionId,
                                              String canal,
                                              String autorizacaoRetorno) {
        var solicitacaoCadastroContaSalario = solicitacaoMapper.map(solicitacaoDTO);
        solicitacaoCadastroContaSalario.setIdTransacao(new BigInteger(transactionId));
        solicitacaoCadastroContaSalario.setCanal(canal);
        solicitacaoCadastroContaSalario.setStatus(Status.PENDENTE);
        solicitacaoCadastroContaSalario.setResultado(Resultado.RECEBIDO);

        for (Cadastro cadastro : solicitacaoCadastroContaSalario.getCadastros()) {
            cadastro.setSolicitacao(solicitacaoCadastroContaSalario);
            cadastro.setSituacao(Resultado.EM_PROCESSAMENTO);
        }

        if (Objects.nonNull(solicitacaoCadastroContaSalario.getConfiguracao()) && Objects.nonNull(autorizacaoRetorno)) {
            solicitacaoCadastroContaSalario.getConfiguracao().setAutorizacaoRetorno(autorizacaoRetorno);
        }
        return solicitacaoCadastroContaSalario;
    }

    public List<CriarContaSalarioResponse> processarSolicitacaoClient(SolicitacaoRequestDTO request,
                                                                      String transactionId,
                                                                      String canal,
                                                                      String autorizacaoRetorno) {

        var solicitacaoCadastroContaSalario = criarNovaSolicitacao(request, transactionId, canal, autorizacaoRetorno);
        solicitacaoCadastroContaSalario.setIdTransacao(new BigInteger(transactionId));
        var response = new ArrayList<CriarContaSalarioResponse>();
        solicitacaoCadastroContaSalario.getCadastros()
                .forEach(cadastroSolicitacao -> response.add(contaSalarioService.criarContaSalario(cadastroSolicitacao, false)));
        return response;
    }

    @IdempotentTransaction
    public IdempotentResponse<SolicitacaoResponseDTO> reprocessarSolicitacao(IdempotentRequest<String> request) {

        log.info("[{}]- Reprocessando solicitacao com transactionId: {} . novoTransactionId: {}",
                request.getTransactionId(), request.getValue(), request.getTransactionId());
        var solicitacaoOriginal = solicitacaoRepository.findByIdTransacao(new BigInteger(request.getValue())).orElseThrow(NotFoundException::new);
        var solicitacaoRequestDTO = solicitacaoMapper.mapToRequest(solicitacaoOriginal);

        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID_SOLICITACAO, request.getTransactionId());

        var idempotentRequest = IdempotentRequest.<SolicitacaoRequestDTO>builder()
                .transactionId(request.getTransactionId())
                .value(solicitacaoRequestDTO)
                .headers(headers)
                .build();

        var retorno = this.processarSolicitacao(idempotentRequest,
                request.getTransactionId(),
                solicitacaoOriginal.getCanal(),
                solicitacaoOriginal.getConfiguracao().getAutorizacaoRetorno());

        log.info("[{}] - Solicitacao {} enviada para reprocessamento com novo transactionId: {}",
                request.getTransactionId(), request.getValue(), request.getTransactionId());
        return retorno;
    }

    @IdempotentTransaction
    public IdempotentResponse<SolicitacaoResponseDTO> agregaRespostasDeCadastroDaSolicitacao(IdempotentAsyncRequest<Cadastro> cadastroRequest){
        Cadastro cadastro = cadastroRepository.findById(cadastroRequest.getValue().getId())
                .orElseThrow(NotFoundException::new);

        Long naoProcessados = cadastroRepository.countBySolicitacaoIdAndProcessado(cadastro.getSolicitacao().getId(), Boolean.FALSE);
        Solicitacao solicitacao = solicitacaoRepository.findByIdTransacaoLock(cadastro.getSolicitacao().getIdTransacao())
                .orElseThrow(NotFoundException::new);

        boolean possuiCadastroEmProcessamento = solicitacao.getCadastros()
                .stream()
                .anyMatch(cadastro1 -> cadastro1.getSituacao().equals(Resultado.EM_PROCESSAMENTO));

        log.info("[{}] - Processamento de agregação da solicitação. naoProcessados=[{}], resultado=[{}]",
                solicitacao.getIdTransacao(), naoProcessados, solicitacao.getResultado().name());

        if(Boolean.FALSE.equals(possuiCadastroEmProcessamento) && naoProcessados == 0 &&
                (Resultado.RECEBIDO.equals(solicitacao.getResultado()) || Resultado.EM_PROCESSAMENTO.equals(solicitacao.getResultado()))){
            log.info("[{}] - Processamento de cadastros da solicitação finalizados. Finalizando solicitação",
                    solicitacao.getIdTransacao());

            Solicitacao solicitacaoFinalizada = finalizaSolicitacao(solicitacao);
            SolicitacaoResponseDTO response =
                    solicitacaoMapper.map(solicitacaoFinalizada);

            log.info("[{}] - Solicitacao finalizada.", solicitacaoFinalizada.getIdTransacao());

            Map<String, String> headers = new HashMap<>();
            headers.put(TRANSACTION_ID_SOLICITACAO, response.idTransacao());
            headers.put(RESULTADO, response.resultado());
            headers.put(STATUS, response.status());
            headers.put(CANAL, response.canal());
            headers.put(EVENT_TYPE, EVENT_TYPE_SOLICITACAO);

            return IdempotentResponse
                    .<SolicitacaoResponseDTO>builder()
                    .value(response)
                    .errorResponse(false)
                    .headers(headers)
                    .build();
        }
        return IdempotentResponse.
                <SolicitacaoResponseDTO>builder()
                .build();
    }

    private Solicitacao finalizaSolicitacao(Solicitacao solicitacao) {
        Solicitacao.SolicitacaoBuilder builder = solicitacao.toBuilder()
                .status(Status.FINALIZADO);

        boolean todosEfetivados = solicitacao.getCadastros().stream().allMatch(cadastro -> cadastro.getSituacao().equals(Resultado.CONCLUIDO));
        boolean todosComErro = solicitacao.getCadastros().stream().allMatch(cadastro -> cadastro.getSituacao().equals(Resultado.ERRO));
        boolean qualquerCritica = solicitacao.getCadastros().stream().anyMatch(cadastro -> !cadastro.getCriticas().isEmpty());

        if(todosComErro){
            builder.resultado(Resultado.ERRO);
        } else if (todosEfetivados) {
            builder.resultado(Resultado.CONCLUIDO);
        } else {
            builder.resultado(Resultado.CONCLUIDO_PARCIALMENTE);
        }

        if(qualquerCritica){
            builder.critica(true);
        }

        Solicitacao solicitacaoAtualizada = builder.build();

        solicitacaoAtualizada = solicitacaoRepository.save(solicitacaoAtualizada);
        metricService.incrementCounter("solicitacao_processada",
                RESULTADO, solicitacaoAtualizada.getResultado().getDescricao());

        return solicitacaoAtualizada;
    }

    public void processarCallbackWebhook(String transactionIdSolicitacao) {
        solicitacaoRepository.findByIdTransacao(new BigInteger(transactionIdSolicitacao))
                .ifPresent(solicitacao -> {
                    if (Status.FINALIZADO.equals(solicitacao.getStatus())
                            && Objects.nonNull(solicitacao.getConfiguracao())
                            &&  Objects.nonNull(solicitacao.getConfiguracao().getUrlWebhook())
                            &&  Objects.isNull(solicitacao.getWebhookHttpStatusCodigo())) {
                        Integer statusCodeWebhook = webhookService.processarRetornoWebhook(solicitacao.getConfiguracao(), solicitacao);
                        solicitacao.setWebhookHttpStatusCodigo(String.valueOf(statusCodeWebhook));
                        log.info("[{}] - Solicitacao atualizada com Http Status Webhook. HttpStatus[{}]", solicitacao.getIdTransacao(), statusCodeWebhook);
                        solicitacaoRepository.save(solicitacao);
                    }
                });
    }
}