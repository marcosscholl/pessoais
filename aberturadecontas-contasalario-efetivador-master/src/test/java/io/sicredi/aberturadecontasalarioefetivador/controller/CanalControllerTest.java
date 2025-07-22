package io.sicredi.aberturadecontasalarioefetivador.controller;


import io.sicredi.aberturadecontasalarioefetivador.dto.CanalRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.TransactionIDObrigatorioException;
import io.sicredi.aberturadecontasalarioefetivador.factories.CanalFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.TransactionIdFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.CanalService;
import io.sicredi.aberturadecontasalarioefetivador.service.HeaderService;
import io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CanalControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CanalService canalService;
    @MockBean
    private HeaderService headerService;
    public static final String CANAL_VALIDO = "CANAL_VALIDO";

    @Test
    @DisplayName("Deve criar o Canal sem documento com sucesso")
    void deveCriarCanalSemDocumentoComSucesso() throws Exception {
        var canal = CanalFactory.canalValido();

        when(canalService.criarCanal(Mockito.any(Canal.class))).thenReturn(canal);

        mockMvc.perform(post("/canal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"" + canal.getNome() + "\",  \"ativo\": true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is(canal.getNome())))
                .andExpect(jsonPath("$.codigo", is(canal.getCodigo().intValue())))
                .andExpect(jsonPath("$.documento", is(canal.getDocumento())))
                .andExpect(jsonPath("$.ativo", is(true)));

        verify(canalService).criarCanal(Mockito.any(Canal.class));
    }

    @Test
    @DisplayName("Deve criar canal com documento com sucesso")
    void deveCriarCanalComDocumentoComSucesso() throws Exception {
        var canal = CanalFactory.canalValido();
        var canalRequestDTO = CanalRequestDTO.builder().nome(canal.getNome()).documento("11111111111").ativo(true).build();

        when(canalService.criarCanal(Mockito.any(Canal.class))).thenReturn(canal);

        mockMvc.perform(post("/canal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objetoString(canalRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is(canal.getNome())))
                .andExpect(jsonPath("$.codigo", is(canal.getCodigo().intValue())))
                .andExpect(jsonPath("$.ativo", is(true)));

        verify(canalService).criarCanal(Mockito.any(Canal.class));
    }

    @Test
    @DisplayName("Deve retornar BadRequest para criação de canal já existente")
    void deveRetornarBadRequestParaCriacaoDeCanalJaExistente() throws Exception {
        when(canalService.criarCanal(Mockito.any(Canal.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do canal já existe."));

        mockMvc.perform(post("/canal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"CANAL_EXISTENTE\", \"ativo\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome do canal já existe."));

        verify(canalService).criarCanal(Mockito.any(Canal.class));
    }

    @Test
    @DisplayName("Deve consultar canal com sucesso")
    void deveConsultarCanalComSucesso() throws Exception {
        var canal = CanalFactory.canalValido();

        when(canalService.consultarCanal(canal.getCodigo())).thenReturn(Optional.of(canal));

        mockMvc.perform(get("/canal/{codigo}", canal.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is(canal.getNome())))
                .andExpect(jsonPath("$.codigo", is(canal.getCodigo().intValue())))
                .andExpect(jsonPath("$.ativo", is(true)));

        verify(canalService).consultarCanal(canal.getCodigo());
    }

    @Test
    @DisplayName("Deve retornar canal vazio quando não encontrado")
    void deveRetornarCanalVazioQuandoNaoEncontrado() throws Exception {
        var codigo = 9999L;

        when(canalService.consultarCanal(codigo)).thenReturn(Optional.empty());

        mockMvc.perform(get("/canal/{codigo}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").doesNotExist())
                .andExpect(jsonPath("$.codigo").doesNotExist())
                .andExpect(jsonPath("$.ativo").doesNotExist());

        verify(canalService).consultarCanal(codigo);
    }

    @Test
    @DisplayName("Deve retornar InternalServerError quando falhar na busca de canal")
    void deveRetornar500QuandFalharNaBuscaDeCanal() throws Exception {
        var codigo = 9999L;

        when(canalService.consultarCanal(codigo))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/canal/{codigo}", codigo))
                .andExpect(status().isInternalServerError());

        verify(canalService).consultarCanal(codigo);
    }

    @Test
    @DisplayName("Deve desativar canal com sucesso")
    void deveDesativarCanalComSucesso() throws Exception {
        var canal = CanalFactory.canalValido();

        when(canalService.desativarCanal(canal.getCodigo())).thenReturn(canal);

        mockMvc.perform(put("/canal/{codigo}/desativar", canal.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(content().string("Canal desativado com sucesso."));

        verify(canalService, times(1)).desativarCanal(canal.getCodigo());
    }

    @Test
    @DisplayName("Deve retornar InternalServerError quando falhar na desativação de canal")
    void deveRetornar500QuandoFalharNaDesativacaoDeCanal() throws Exception {
        var canal = CanalFactory.canalValido();

        when(canalService.desativarCanal(canal.getCodigo()))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(put("/canal/{codigo}/desativar", canal.getCodigo()))
                .andExpect(status().isInternalServerError());

        verify(canalService, times(1)).desativarCanal(canal.getCodigo());
    }

    @Test
    @DisplayName("Deve validar TransactionId")
    void deveValidarTransactionId() throws Exception {
        var transactionId = TransactionIdFactory.transactionIdValido(1234L);

        doNothing().when(headerService).validarHeaderSolicitacao(transactionId, CANAL_VALIDO);

        mockMvc.perform(post("/canal/validar/{transactionId}/{canal}", transactionId, CANAL_VALIDO))
                .andExpect(status().isOk());

        verify(headerService).validarHeaderSolicitacao(transactionId, CANAL_VALIDO);
    }

    @Test
    @DisplayName("Deve retornar erro na validação do TransactionId")
    void deveRetornarErroNaValidacaoDoTransactionId() throws Exception {
        var transactionId = "XPTO";

        doThrow(TransactionIDObrigatorioException.class).when(headerService).validarHeaderSolicitacao(transactionId, CANAL_VALIDO);

        mockMvc.perform(post("/canal/validar/{transactionId}/{canal}", transactionId, CANAL_VALIDO))
                .andExpect(status().isUnprocessableEntity());

        verify(headerService, times(1)).validarHeaderSolicitacao(transactionId, CANAL_VALIDO);
    }
}