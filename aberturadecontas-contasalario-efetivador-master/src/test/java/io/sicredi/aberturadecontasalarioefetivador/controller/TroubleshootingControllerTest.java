package io.sicredi.aberturadecontasalarioefetivador.controller;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.factories.*;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.aberturadecontasalarioefetivador.service.ConsultarContaSalarioService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
import io.sicredi.aberturadecontasalarioefetivador.service.WebhookService;
import io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TroubleshootingControllerTest {

    public static final String CODIGO_CONVENIO = "3AO";
    public static final String DOCUMENTO = "36185900084";
    @MockBean
    private SolicitacaoService solicitacaoService;
    @MockBean
    private WebhookService webhookService;
    @MockBean
    private SolicitacaoRepository solicitacaoRepository;
    @MockBean
    private ConsultarContaSalarioService consultarContaSalarioService;
    @Autowired
    private MockMvc mvc;
    private static final String TRANSACTIONID = "2025022743671603934576222386";
    private static final String URL_CONSULTA_SOLICITACAO = "/troubleshooting/solicitacao/";

    @Test
    @DisplayName("Deve processar solicitacao de abertura de conta salário manualmente")
    void deveProcessarSolicitacaoDeAberturaDeContaSalarioManualmente() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var criarContaSalarioResponseList = new ArrayList<CriarContaSalarioResponse>();
        IntStream.range(0, solicitacao.getCadastros().size())
                        .forEach(index -> criarContaSalarioResponseList
                                .add(CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, index)));

        when(solicitacaoService.processarSolicitacaoClient(any(SolicitacaoRequestDTO.class), anyString(), anyString(), anyString()))
                .thenReturn(criarContaSalarioResponseList);

        mvc.perform(post("/troubleshooting/solicitacao")
                .header("TransactionID", "")
                .header("Canal", "")
                .header("Authorization-callback", "")
                .content(TestUtils.objetoString(solicitacaoRequestDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(criarContaSalarioResponseList.size())))
                .andExpect(jsonPath("$[*].criarContaSalarioResponse.numCPF",
                        everyItem(in(solicitacao.getCadastros().stream().map(Cadastro::getCpf).toList()))));

        verify(solicitacaoService, times(1))
                .processarSolicitacaoClient(any(SolicitacaoRequestDTO.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar InternalServerError quando ocorrer erro genérico ao processar solicitacao manualmente")
    void deveRetornarInternalServerErrorQuandoOcorrerErroGenericoAoProcessarSolicitacaoManualmente() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();

        doThrow(new ResponseStatusException(HttpStatusCode.valueOf(500)))
                .when(solicitacaoService).processarSolicitacaoClient(any(SolicitacaoRequestDTO.class), anyString(), anyString(), anyString());

        mvc.perform(post("/troubleshooting/solicitacao")
                        .header("TransactionID", TRANSACTIONID)
                        .header("Canal", "FOLHA_IB")
                        .header("Authorization-callback", "abcd")
                        .content(TestUtils.objetoString(solicitacaoRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(solicitacaoService, times(1))
                .processarSolicitacaoClient(any(SolicitacaoRequestDTO.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve reprocessar uma solicitação através do TransactionId")
    void deveReprocessarUmaSolicitacaoAtravesDoTransactionId() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var solicitacaoResponseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacao);
        IdempotentResponse<SolicitacaoResponseDTO> build = IdempotentResponse.<SolicitacaoResponseDTO>builder().value(solicitacaoResponseDTO).build();

        when(solicitacaoService.reprocessarSolicitacao(any()))
                .thenReturn(build);

        mvc.perform(post("/troubleshooting/solicitacao/reprocessar")
                        .content("2025022743671603934576222386")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cadastros", hasSize(solicitacaoResponseDTO.cadastros().size())))
                .andExpect(jsonPath("$[*].cadastros.cpf",
                        everyItem(in(solicitacao.getCadastros().stream().map(Cadastro::getCpf).toList()))));
    }

    @Test
    @DisplayName("Deve consultar solicitacao solicitacao completa")
    void deveConsultarSolicitacaoCompleta() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var response = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);

        when(solicitacaoService.consultarSolicitacaoCompleta(new BigInteger(TRANSACTIONID))).thenReturn(Optional.of(response));

        mvc.perform(get(URL_CONSULTA_SOLICITACAO+TRANSACTIONID)
                        .content(TestUtils.objetoString(response))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTransacao").value(response.getIdTransacao()));

        verify(solicitacaoService, times(1)).consultarSolicitacaoCompleta(new BigInteger(TRANSACTIONID));
    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar solicitação buscada")
    void deveRetornarVazioQuandoNaoEncontrarSolicitacaoBuscada() throws Exception {
        when(solicitacaoService.consultarSolicitacaoCompleta(new BigInteger(TRANSACTIONID))).thenReturn(Optional.empty());

        mvc.perform(get(URL_CONSULTA_SOLICITACAO+TRANSACTIONID)
                        .content(TestUtils.objetoString("{}"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(solicitacaoService, times(1)).consultarSolicitacaoCompleta(new BigInteger(TRANSACTIONID));
    }

    @Test
    @DisplayName("Deve receber uma transação de resposta no webhook (simulação)")
    void deveReceberUmaTransacaoDeRespostaNoWebhook() throws Exception {
        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);
        var solicitacaoResponseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTO(solicitacao);

        when(solicitacaoRepository.findByIdTransacao(any(BigInteger.class))).thenReturn(Optional.of(solicitacao));

        mvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objetoString(solicitacaoResponseDTO)))
                .andExpect(status().isAccepted());

        verify(solicitacaoRepository, times(1)).findByIdTransacao(any(BigInteger.class));
    }

    @Test
    @DisplayName("Deve realizar reenvio de resposta para o webhook")
    void deveRealizarReenvioDeRespostaParaOWebhook() throws Exception {

        var solicitacaoRequestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(solicitacaoRequestDTO);

        when(solicitacaoRepository.findByIdTransacao(any(BigInteger.class))).thenReturn(Optional.of(solicitacao));
        when(webhookService.processarRetornoWebhook(any(Configuracao.class), any(Solicitacao.class)))
                .thenReturn(200);

        mvc.perform(post("/troubleshooting/webhook/{transactionId}", TRANSACTIONID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(solicitacaoRepository, times(1)).findByIdTransacao(any(BigInteger.class));
        verify(webhookService, times(1)).processarRetornoWebhook(any(Configuracao.class), any(Solicitacao.class));
    }

    @Test
    @DisplayName("Deve Consultar Documento Conta Salario e Retornar na lista quando encontrar")
    void deveConsultarContaSalarioDocumentoContaSalarioERetornarNaListaQuandoEncontrar() throws Exception {
        List<ConsultarContaSalarioResponseDTO> resultadoConsultaContaSalario =
                ConsultarContaSalarioResponseDTOFactory.consultarContaSalarioResponseDTO();

        when(consultarContaSalarioService.consultarContaSalario(anyString(), anyString(), anyString()))
                .thenReturn(resultadoConsultaContaSalario);

        mvc.perform(MockMvcRequestBuilders.get("/troubleshooting/conta-salario/documento/36185900084/3AO")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("VALOR DEFAULT MOCK")))
                .andExpect(jsonPath("$[0].convenio.codigo", is(CODIGO_CONVENIO)))
                .andExpect(jsonPath("$[0].alteracoes", hasSize(3)));

        verify(consultarContaSalarioService).consultarContaSalario(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar critica CPF Invalido")
    public void deveRetornarCriticaCPFInvalido() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/troubleshooting/conta-salario/documento/03104322008/" + CODIGO_CONVENIO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O campo 'documento' deve conter um CPF válido"));
    }

    @Test
    @DisplayName("Deve retornar crítica tamanho Codigo Fonte")
    public void deveRetornarCriticaTamanhoodigoFonte() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/troubleshooting/conta-salario/documento/"+ DOCUMENTO +"/" + DOCUMENTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O campo 'convenio' deve ter no máximo 7 caracteres"));
    }
}