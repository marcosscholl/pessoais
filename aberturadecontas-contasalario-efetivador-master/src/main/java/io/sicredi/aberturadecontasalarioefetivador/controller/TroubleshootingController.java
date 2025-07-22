package io.sicredi.aberturadecontasalarioefetivador.controller;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoWebhookResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.aberturadecontasalarioefetivador.service.ConsultarContaSalarioService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
import io.sicredi.aberturadecontasalarioefetivador.service.TransactionIdService;
import io.sicredi.aberturadecontasalarioefetivador.service.WebhookService;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping()
@AllArgsConstructor
@Validated
public class TroubleshootingController {
    private static final String CABECALHO_TRANSACTION_ID = "TransactionId";
    private static final String CABECALHO_CANAL = "Canal";
    private static final String CABECALHO_AUTHORIZATION_CALLBACK = "Authorization-Callback";
    private static final String CABECALHO_INTERNO_TRANSACTION_ID = "transactionIdSolicitacao";

    private final SolicitacaoService solicitacaoService;
    private final SolicitacaoRepository solicitacaoRepository;
    private final WebhookService webhookService;
    private final TransactionIdService transactionIdService;
    private final ConsultarContaSalarioService consultarContaSalarioService;

    @PostMapping(path = "/troubleshooting/solicitacao")
    @Operation(summary = "Realização de processamento (criação das contas) manual de solicitacao de cadastros de Conta Salário")
    public ResponseEntity<List<CriarContaSalarioResponse>> solicitacaoContaSalarioService(@RequestBody SolicitacaoRequestDTO solicitacaoRequestDTO,
                                                                                         @RequestHeader(name = CABECALHO_TRANSACTION_ID) String transactionIdSolicitacao,
                                                                                         @RequestHeader(name = CABECALHO_CANAL) String canal,
                                                                                         @RequestHeader(name = CABECALHO_AUTHORIZATION_CALLBACK, required = false) String autorizacaoRetorno) {
        return ResponseEntity.ok().body(solicitacaoService.processarSolicitacaoClient(solicitacaoRequestDTO, transactionIdSolicitacao, canal, autorizacaoRetorno));
    }

    @PostMapping(path = "/troubleshooting/solicitacao/reprocessar")
    @Operation(summary = "Realização do reprocessamento (criação das contas) de uma solicitação de cadastros de Conta Salário pré existente")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<SolicitacaoResponseDTO> reprocessarSolicitacao(@RequestBody @NotBlank String transactionId){
        var novoTransactionId = transactionIdService.criaTransactionId();

        Map<String, String> headers = new HashMap<>();
        headers.put(CABECALHO_INTERNO_TRANSACTION_ID, novoTransactionId);

        var idempotentRequest = IdempotentRequest.<String>builder()
                .transactionId(novoTransactionId)
                .value(transactionId)
                .headers(headers)
                .build();

        var retorno = solicitacaoService.reprocessarSolicitacao(idempotentRequest).getValue();
        return ResponseEntity.ok().body(retorno);
    }

    @GetMapping(path = "/troubleshooting/solicitacao/{TransactionId}")
    @Operation(summary = "Busca completa de solicitação de cadastros de Contas Salário")
    public ResponseEntity<?> consultarSolicitacaoContaSalarioService(@PathVariable("TransactionId") BigInteger transactionId) {
        Optional<Solicitacao> solicitacao = solicitacaoService.consultarSolicitacaoCompleta(transactionId);
        if (solicitacao.isPresent()) return ResponseEntity.ok().body(solicitacao);
        return ResponseEntity.ok().body("{}");
    }

    @PostMapping(path = "/webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Exposição interna de webhook de cadastros de Conta Salário")
    public void webhook(@RequestBody SolicitacaoWebhookResponseDTO resultado) {
        log.info("[{}] - RECEBIDO EVENTO WEBHOOK : {}", resultado.idTransacao(), resultado);
        solicitacaoRepository.findByIdTransacao(new BigInteger(resultado.idTransacao()))
                .ifPresent(solicitacao ->
                        log.info("[{}] - WEBHOOK CONSULTA SOLICITACAO : {}", resultado.idTransacao(), solicitacao));
    }

    @PostMapping(path = "/troubleshooting/webhook/{transactionId}")
    @Operation(summary = "Realização de processamento manual de resultado de solicitacao de cadastros de Conta Salário por Webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void webhookConsulta(@PathVariable("transactionId") BigInteger transactionId) {
        solicitacaoRepository.findByIdTransacao(transactionId)
                .ifPresent(solicitacao -> {
                    log.info("[{}] - ENVIA EVENTO WEBHOOK", solicitacao.getIdTransacao());
                    webhookService.processarRetornoWebhook(solicitacao.getConfiguracao(), solicitacao);
                });
    }

    @GetMapping(path = "/troubleshooting/conta-salario/documento/{documento}/{convenio}")
    @Operation(summary = "Busca detalhes de Conta Salário por documento e convênio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    public List<ConsultarContaSalarioResponseDTO> consultarContaSalario(@PathVariable("documento")
                                                                        @CPF(message = "O campo 'documento' deve conter um CPF válido")
                                                                        String documento,
                                                                        @PathVariable("convenio")
                                                                        @Size(max = 7, message = "O campo 'convenio' deve ter no máximo 7 caracteres")
                                                                        String convenio) {
        return consultarContaSalarioService.consultarContaSalario(documento, convenio, "");
    }
}
