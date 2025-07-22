package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoResponseDTOFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeradorSolicitacaoContaSalarioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SolicitacaoContaSalarioController solicitacaoContaSalarioController;
    @MockBean
    private TransactionIdController transactionIdController;

    @Test
    @DisplayName("Deve criar solicitação com cadastro mínimo")
    void deveCriarSolicitacaoComCadastroMinimo() throws Exception {
        var solicitacaoResponseDTO =
                SolicitacaoResponseDTOFactory.solicitacaoResponseDTOPendenteCadastroMinimoSucesso();
        var body = ResponseEntity.accepted().body(solicitacaoResponseDTO);

        doReturn(body).when(solicitacaoContaSalarioController)
                .solicitacao(any(SolicitacaoRequestDTO.class), anyString(), anyString(), any());
        when(transactionIdController.transactionId())
                .thenReturn(ResponseEntity.ok(solicitacaoResponseDTO.idTransacao()));

        mockMvc.perform(post("/geradorsolicitacao")
                        .param("quantidadeCadastros", "1")
                        .param("apenasCPFRegular", "true")
                        .param("cadastroMinimo", "false")
                        .param("urlWebhook", "http://localhost:8080"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.idTransacao", is(solicitacaoResponseDTO.idTransacao())))
                .andExpect(jsonPath("$.canal", is("FOLHA_IB")))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.resultado", is("RECEBIDO")))
                .andExpect(jsonPath("$.numCooperativa", is("0167")))
                .andExpect(jsonPath("$.numAgencia", is("17")));

        verify(solicitacaoContaSalarioController, Mockito.times(1))
                .solicitacao(any(SolicitacaoRequestDTO.class), anyString(), anyString(), any());
        verify(transactionIdController, times(1)).transactionId();

    }

    @Test
    @DisplayName("Deve criar solicitação com cadastro completo")
    void deveCriarSolicitacaoComCadastroCompleto() throws Exception {
        var solicitacaoResponseDTO =
                SolicitacaoResponseDTOFactory.solicitacaoResponseDTOPendenteCadastroCompletoSucesso();
        var body = ResponseEntity.accepted().body(solicitacaoResponseDTO);

        doReturn(body).when(solicitacaoContaSalarioController)
                .solicitacao(any(SolicitacaoRequestDTO.class), anyString(), anyString(), any());
        when(transactionIdController.transactionId())
                .thenReturn(ResponseEntity.ok(solicitacaoResponseDTO.idTransacao()));

        mockMvc.perform(post("/geradorsolicitacao")
                        .param("quantidadeCadastros", "1")
                        .param("apenasCPFRegular", "true")
                        .param("cadastroMinimo", "true"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.idTransacao", is(solicitacaoResponseDTO.idTransacao())))
                .andExpect(jsonPath("$.canal", is("FOLHA_IB")))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.resultado", is("RECEBIDO")))
                .andExpect(jsonPath("$.numCooperativa", is("0167")))
                .andExpect(jsonPath("$.numAgencia", is("17")))
                .andExpect(jsonPath("$.cpfFontePagadora", is("03104322007")));

        verify(solicitacaoContaSalarioController, Mockito.times(1))
                .solicitacao(any(SolicitacaoRequestDTO.class), anyString(), anyString(), any());
        verify(transactionIdController, times(1)).transactionId();

    }

}