package io.sicredi.aberturadecontasalarioefetivador.controller;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontasalarioefetivador.dto.*;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.AgenciaECooperativaNaoCorrespondentesException;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.CanalNaoEncontradoOuInatvoException;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoRequestDTOFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoResponseDTOFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.HeaderService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
import io.sicredi.aberturadecontasalarioefetivador.service.WebhookService;
import io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentEvent;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransactionDuplicatedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitacaoContaSalarioControllerTest {

    @MockBean
    private SolicitacaoService solicitacaoService;
    @MockBean
    HeaderService headerService;
    @MockBean
    private WebhookService webhookService;
    @Autowired
    private MockMvc mvc;
    private static final String CABECALHO_EXTERNO_TRANSACTION_ID = "TransactionId";
    private static final String CABECALHO_EXTERNO_CANAL = "Canal";
    private static final String TOPICO_PROCESSAMENTO_CADASTRO_UNITARIO = "aberturadecontas-contasalario-efetivador-cadastros-v1";
    private static final String CABECALHO_INTERNO_TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String CABECALHO_INTERNO_TRANSACTION_ID = "transactionId";
    private static final String CABECALHO_AUTHORIZATION_CALLBACK = "Authorization-Callback";
    private static final String AUTORIZACAO_RETORNO = "sWa1LnR4XqHrDDQp5WaRGrKXYDcyLBvk";
    private static final String TRANSACTIONID = "2024112843678422634659852591";
    private static final String CANAL_EXTERNO = "EXTERNO";
    private static final String CONSULTA_SOLICITACAO_SEM_RETORNO = "{}";
    private static final String URL_CONSULTA_SOLICITACAO = "/solicitacao/";
    private static final String DOCUMENTO_VALOR = "12345678901";
    private static final String PARAM_CANAL = "canal";
    private static final String PARAM_DOCUMENTO = "documento";
    private static final String URL_VALIDAR_TRANSACTION_ID = "/validar/{TransactionId}";

    @Test
    @DisplayName("Deve criar solicitação")
    void deveCriarSolicitacao() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        solicitacaoRequestDTO = solicitacaoRequestDTO.toBuilder().numAgencia("A2").configuracao(null).build();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var solicitacaoAgenciaAlfa = solicitacao.toBuilder().numAgencia("A2").build();
        var responseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacaoAgenciaAlfa);
        var idempotentRequest = mapIdempotenteRequest(solicitacaoRequestDTO);
        var idempotentResponse = mapIdempotentResponse(solicitacaoAgenciaAlfa, idempotentRequest, responseDTO, false);

        when(solicitacaoService.processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, AUTORIZACAO_RETORNO))
                .thenReturn(idempotentResponse);
        doNothing().when(headerService).validarHeaderSolicitacao(anyString(), anyString());

        mvc.perform(MockMvcRequestBuilders
                .post("/solicitacao")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.idTransacao").value(responseDTO.idTransacao()));

        verify(solicitacaoService, times(1))
                .processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, AUTORIZACAO_RETORNO);
        verify(headerService, times(1))
                .validarHeaderSolicitacao(anyString(), anyString());
        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Deve retornar BadRequest com mensagem de erro quando solicitacao possuir cadastros com o mesmo CPF")
    void deveRetornarBadRequestComEMensagemDeErroQuandoSolicitacaoPossuirCadastrosComMesmoCPF() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var cpf = solicitacaoRequestDTO.cadastros().getFirst().cpf();
        var cadastros = new ArrayList<CadastroRequestDTO>();
        solicitacaoRequestDTO.cadastros().forEach(cadastroRequestDTO -> cadastros.add(cadastroRequestDTO.toBuilder()
                .cpf(cpf).build()));
        solicitacaoRequestDTO = solicitacaoRequestDTO.toBuilder().cadastros(cadastros).build();

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cadastros").value("A solicitação não pode possuir mais de um cadastro com o mesmo CPF"));

        verifyNoInteractions(solicitacaoService);
        verifyNoInteractions(headerService);
        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Deve retornar BadRequest e mensagem de erro quando solicitação não possuir corpo na requisição")
    void deveRetornarBadRequestEMensagemDeErroParaRequisicaoSemCorpo() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O corpo da requisição é obrigatório e está ausente."));

        verifyNoInteractions(solicitacaoService);
        verifyNoInteractions(headerService);
        verifyNoInteractions(webhookService);
    }
    
    @Test
    @DisplayName("Deve retornar UnprocessableEntity quando cooperativa e agencia enviados não forem correspondentes")
    void deveRetornarUnprocessableEntityParaCooperativaEAgenciaNaoCorrespondetes() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        solicitacaoRequestDTO = solicitacaoRequestDTO.toBuilder().configuracao(null).build();
        var idempotentRequest = mapIdempotenteRequest(solicitacaoRequestDTO);
        var exception = new AgenciaECooperativaNaoCorrespondentesException();

        doThrow(exception)
                .when(solicitacaoService).processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, AUTORIZACAO_RETORNO);
        doNothing().when(headerService).validarHeaderSolicitacao(anyString(), anyString());

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isUnprocessableEntity());

        verify(solicitacaoService, times(1))
                .processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, AUTORIZACAO_RETORNO);
        verify(headerService, times(1))
                .validarHeaderSolicitacao(anyString(), anyString());
        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Deve criar solicitação sem autorização de retorno")
    void deveCriarSolicitacaoSemAutorizacaoRetorno() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var responseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacao);
        var idempotentRequest = mapIdempotenteRequest(solicitacaoRequestDTO);
        var idempotentResponse = mapIdempotentResponse(solicitacao, idempotentRequest, responseDTO, false);

        when(webhookService.webhookConectividade(any(Configuracao.class))).thenReturn(true);
        when(solicitacaoService.processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, null))
                .thenReturn(idempotentResponse);

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.idTransacao").value(responseDTO.idTransacao()));

        verify(webhookService, times(1)).webhookConectividade(any(Configuracao.class));
        verify(solicitacaoService, times(1))
                .processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, null);
    }

    @Test
    @DisplayName("Não deve criar solicitacao com conflito de TransactionID")
    void naoDeveCriarSolicitacaoComConflitoDeTransactionId() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var idempotentRequest = mapIdempotenteRequest(requestDTO);

        doThrow(IdempotentTransactionDuplicatedException.class)
                .when(headerService).validarHeaderSolicitacao(TRANSACTIONID, CANAL_EXTERNO);

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isConflict());

        verify(headerService, times(1)).validarHeaderSolicitacao(TRANSACTIONID, CANAL_EXTERNO);
        verify(solicitacaoService, times(0)).processarSolicitacao(idempotentRequest, TRANSACTIONID, CANAL_EXTERNO, AUTORIZACAO_RETORNO);
    }

    @Test
    @DisplayName("Não deve criar solicitação com representante igual ao titular")
    void naoDeveCriarSolicitacaoComRepresentanteIgualTitular() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var cadastroInvalido = requestDTO.cadastros().getFirst().toBuilder()
                .cpf("25691368087")
                .representante(RepresentanteDTO.builder()
                        .cpf("25691368087").build()).build();
        requestDTO.cadastros().add(cadastroInvalido);
        var idempotentRequest = mapIdempotenteRequest(requestDTO);

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"cadastros[2].representante\":\"O titular nÃ£o pode ser o seu prÃ³prio representante\"}"));

        verify(solicitacaoService, times(0)).processarSolicitacao(idempotentRequest, TRANSACTIONID, CANAL_EXTERNO, AUTORIZACAO_RETORNO);
    }

    @Test
    @DisplayName("Deve retornar InternalServerError quando ocorrer erro genérico ao criar solicitação")
    void deveRetornarInternalServerErrorQuandoOcorrerErroGenericoAoCriarSolicitacao() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var responseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacao);
        var idempotentRequest = mapIdempotenteRequest(solicitacaoRequestDTO);
        var idempotentResponse = mapIdempotentResponse(solicitacao, idempotentRequest, responseDTO, true);

        when(webhookService.webhookConectividade(any(Configuracao.class))).thenReturn(true);
        when(solicitacaoService.processarSolicitacao(idempotentRequest, TRANSACTIONID, CANAL_EXTERNO, AUTORIZACAO_RETORNO))
                .thenReturn(idempotentResponse);

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isInternalServerError());

        verify(webhookService, times(1)).webhookConectividade(any(Configuracao.class));
        verify(solicitacaoService, times(1)).processarSolicitacao(idempotentRequest, TRANSACTIONID, CANAL_EXTERNO, AUTORIZACAO_RETORNO);
    }

    @Test
    @DisplayName("Deve retornar erro de validação de Webhook")
    void deveRetornarErroValidacaoWebhook() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();

        when(webhookService.webhookConectividade(any(Configuracao.class))).thenReturn(false);

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.urlWebhook").value("Não foi possível estabelecer conexão com a URL de webhook informada"));

        verify(webhookService, times(1)).webhookConectividade(any(Configuracao.class));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando configuração do Webhook for vazia")
    void deveRetornarBadRequestQuandoConfiguracaoDoWebhookForVazia() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        requestDTO = requestDTO.toBuilder().configuracao(ConfiguracaoDTO.builder().build()).build();

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Deve retornar solicitação encontrada")
    void deveRetornarSolicitacaoEncontrada() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var responseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacao);

        when(solicitacaoService.consultarSolicitacao(new BigInteger(TRANSACTIONID))).thenReturn(responseDTO);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_CONSULTA_SOLICITACAO+TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .content(TestUtils.objetoString(responseDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTransacao").value(responseDTO.idTransacao()));

        verify(solicitacaoService, times(1)).consultarSolicitacao(new BigInteger(TRANSACTIONID));
    }

    @Test
    @DisplayName("Deve retornar vazio quando a solicitacão buscada não for encontrada")
    void deveRetornarVazioNaSolicitacaoQuandoNaoEncontrar() throws Exception {
        when(solicitacaoService.consultarSolicitacao(new BigInteger(TRANSACTIONID)))
                .thenReturn(SolicitacaoResponseDTO.builder().build());

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_CONSULTA_SOLICITACAO+TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .content(CONSULTA_SOLICITACAO_SEM_RETORNO)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk());

        verify(solicitacaoService, times(1)).consultarSolicitacao(new BigInteger(TRANSACTIONID));
    }

    @Test
    @DisplayName("Deve retornar vazio na busca de solicitação quando ocorrer erro")
    void deveRetornarVazioNaSolicitacaoQuandoOcorrerErro() throws Exception {
        when(solicitacaoService.consultarSolicitacao(new BigInteger(TRANSACTIONID))).thenThrow(NotFoundException.class);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_CONSULTA_SOLICITACAO +TRANSACTIONID)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .content(CONSULTA_SOLICITACAO_SEM_RETORNO)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isInternalServerError());

        verify(solicitacaoService).consultarSolicitacao(new BigInteger(TRANSACTIONID));
    }

    @Test
    @DisplayName("Deve validar TransactionId com Canal")
    void deveValidarTransactionIdComCanal() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(URL_VALIDAR_TRANSACTION_ID, TRANSACTIONID)
                        .param(PARAM_CANAL, CANAL_EXTERNO))
                .andExpect(status().isOk());

        verify(headerService).validarHeaderSolicitacao(TRANSACTIONID, CANAL_EXTERNO);
    }

    @Test
    @DisplayName("Deve validar TransactionId com documento")
    void deveValidarTransactionIdComDocumento() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(URL_VALIDAR_TRANSACTION_ID, TRANSACTIONID)
                        .param(PARAM_DOCUMENTO, DOCUMENTO_VALOR))
                .andExpect(status().isOk());

        verify(headerService).validarTransactionIdPorCodigoEDocumento(TRANSACTIONID, DOCUMENTO_VALOR);
    }

    @Test
    @DisplayName("Deve validar TransactionId com canal e documento")
    void deveValidarTransactionIdComCanalEDocumento() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(URL_VALIDAR_TRANSACTION_ID, TRANSACTIONID)
                        .param(PARAM_CANAL, CANAL_EXTERNO)
                        .param(PARAM_DOCUMENTO, DOCUMENTO_VALOR))
                .andExpect(status().isOk());

        verify(headerService).validarTransactionIdPorCodigoEDocumentoECanal(TRANSACTIONID, CANAL_EXTERNO, DOCUMENTO_VALOR);
    }

    @Test
    @DisplayName("Deve retornar UnprocessableEntity quando não forem enviados nem o canal nem o documento")
    void deveRetornarUnprocessableEntityQuandoCanalEDocumentoNaoForemEnviados() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(URL_VALIDAR_TRANSACTION_ID, TRANSACTIONID))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(headerService);
    }

    @Test
    @DisplayName("Deve retornar erro quando HeaderService lançar exceção")
    void deveRetornarErroQuandoHeaderServiceLancaExcecao() throws Exception {
        doThrow(new CanalNaoEncontradoOuInatvoException())
                .when(headerService).validarHeaderSolicitacao(TRANSACTIONID, CANAL_EXTERNO);

        mvc.perform(MockMvcRequestBuilders.post(URL_VALIDAR_TRANSACTION_ID, TRANSACTIONID)
                        .param(PARAM_CANAL, CANAL_EXTERNO))
                .andExpect(status().isUnprocessableEntity());

        verify(headerService).validarHeaderSolicitacao(TRANSACTIONID, CANAL_EXTERNO);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando header canal não for enviado")
    void deveRetornarBadRequestQuandoHeaderCanalNaoForEnviado() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_TRANSACTION_ID, TRANSACTIONID)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("{\"header\":\"Canal é obrigatório\"}".getBytes(StandardCharsets.UTF_8)));

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando header TransactionID não for enviado")
    void deveRetornarBadRequestQuandoHeaderTransactionIdNaoForEnviado() throws Exception {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();

        mvc.perform(MockMvcRequestBuilders
                        .post("/solicitacao")
                        .content(TestUtils.objetoString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO)
                        .header(CABECALHO_AUTHORIZATION_CALLBACK, AUTORIZACAO_RETORNO))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("{\"header\":\"TransactionId é obrigatório\"}".getBytes(StandardCharsets.UTF_8)));

        verifyNoInteractions(solicitacaoService);
    }

    private static IdempotentRequest<SolicitacaoRequestDTO> mapIdempotenteRequest(SolicitacaoRequestDTO request) {
        Map<String, String> headers = new HashMap<>();
        headers.put(CABECALHO_INTERNO_TRANSACTION_ID_SOLICITACAO, TRANSACTIONID);
        return IdempotentRequest.<SolicitacaoRequestDTO>builder()
                .transactionId(TRANSACTIONID)
                .value(request)
                .headers(headers)
                .build();
    }

    private static IdempotentResponse<SolicitacaoResponseDTO> mapIdempotentResponse(Solicitacao requestJPA,
                                                                                    IdempotentRequest<SolicitacaoRequestDTO> idempotentRequest,
                                                                                    SolicitacaoResponseDTO responseDTO,
                                                                                    boolean errorResponse) {
        List<IdempotentEvent<?>> eventos = new ArrayList<>();
        requestJPA.getCadastros().forEach(cadastro -> {
            Map<String, String> headers = new HashMap<>(idempotentRequest.getHeaders());
            headers.put(CABECALHO_INTERNO_TRANSACTION_ID, idempotentRequest.getTransactionId()
                    .concat(cadastro.getId().toString())
                    .concat("1"));

            eventos.add(IdempotentEvent.<Cadastro>builder()
                    .value(cadastro)
                    .headers(headers)
                    .topic(TOPICO_PROCESSAMENTO_CADASTRO_UNITARIO)
                    .build());
        });
        return IdempotentResponse
                .<SolicitacaoResponseDTO>builder()
                .value(responseDTO)
                .errorResponse(errorResponse)
                .events(eventos)
                .headers(idempotentRequest.getHeaders())
                .build();
    }
}