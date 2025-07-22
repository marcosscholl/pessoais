package io.sicredi.aberturadecontasalarioefetivador.controller;

import br.com.sicredi.framework.web.spring.exception.BadGatewayException;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConfiguracaoDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebhookException;
import io.sicredi.aberturadecontasalarioefetivador.service.HeaderService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
import io.sicredi.aberturadecontasalarioefetivador.service.WebhookService;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SolicitacaoContaSalarioController {

    private static final String CABECALHO_TRANSACTION_ID = "TransactionId";
    private static final String CABECALHO_CANAL = "Canal";
    private static final String CABECALHO_INTERNO_TRANSACTION_ID = "transactionIdSolicitacao";
    private static final String CABECALHO_AUTHORIZATION_CALLBACK = "Authorization-Callback";
    private final HeaderService headerService;
    private final SolicitacaoService solicitacaoService;
    private final WebhookService webhookService;

    @GetMapping(path = "/solicitacao/{TransactionId}")
    @Operation(summary = "Busca de solicitação de cadastros de Contas Salário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    public SolicitacaoResponseDTO solicitacao(@PathVariable("TransactionId") BigInteger transactionId,
                                              @RequestHeader(name = CABECALHO_CANAL) String canal) {
        headerService.validarHeaderSolicitacao(String.valueOf(transactionId), canal);
        return solicitacaoService.consultarSolicitacao(transactionId);
    }

    @PostMapping(path = "/solicitacao")
    @Operation(summary = "Realiza solicitação de cadastro de Contas Salário")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> solicitacao(@Valid @RequestBody SolicitacaoRequestDTO solicitacaoRequestDTO,
                                              @RequestHeader(name = CABECALHO_TRANSACTION_ID) String transactionIdSolicitacao,
                                              @RequestHeader(name = CABECALHO_CANAL) String canal,
                                              @RequestHeader(name = CABECALHO_AUTHORIZATION_CALLBACK, required = false) String autorizacaoRetorno) {

        headerService.validarHeaderSolicitacao(transactionIdSolicitacao, canal);

        if (Objects.nonNull(solicitacaoRequestDTO.configuracao())) {
            validarWebhook(solicitacaoRequestDTO.configuracao());
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(CABECALHO_INTERNO_TRANSACTION_ID, transactionIdSolicitacao);
        var idempotentRequest = IdempotentRequest.<SolicitacaoRequestDTO>builder()
                .transactionId(transactionIdSolicitacao)
                .value(solicitacaoRequestDTO)
                .headers(headers)
                .build();

        var retorno = solicitacaoService.processarSolicitacao(idempotentRequest, transactionIdSolicitacao, canal, autorizacaoRetorno);
        if(retorno.isErrorResponse()){
            throw new BadGatewayException();
        }
        return ResponseEntity.accepted().body(retorno.getValue());
    }

    @PostMapping("/validar/{TransactionId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> validarTransactionIdCanalDocumento(@Valid @PathVariable("TransactionId") String transactionId,
                                                           @RequestParam(required = false) String canal,
                                                           @RequestParam(required = false) String documento) {
        if (canal != null && documento == null) {
            headerService.validarHeaderSolicitacao(transactionId, canal);
        } else if (documento != null && canal == null) {
            headerService.validarTransactionIdPorCodigoEDocumento(transactionId, documento);
        } else if (documento != null) {
            headerService.validarTransactionIdPorCodigoEDocumentoECanal(transactionId, canal, documento);
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
        return ResponseEntity.ok().build();
    }

    private void validarWebhook(ConfiguracaoDTO configuracaoDTO) {
        if (StringUtils.isBlank(configuracaoDTO.urlWebhook())) {
            throw new WebhookException("Não foi possível estabelecer conexão com a URL de webhook informada");
        }

        boolean conectado = webhookService.webhookConectividade(Configuracao.builder()
                .urlWebhook(configuracaoDTO.urlWebhook())
                .portaHttp(configuracaoDTO.portaHttp())
                .build());
        if (!conectado) {
            throw new WebhookException("Não foi possível estabelecer conexão com a URL de webhook informada");
        }
    }

}
