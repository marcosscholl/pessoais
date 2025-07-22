package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.service.TransactionIdService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionIdControllerTest {

    @MockBean
    private TransactionIdService transactionIdService;
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Deve gerar TransactionID")
    void deveGerarTransactionId() throws Exception {
        when(transactionIdService.criaTransactionId()).thenReturn("2025022743671603934576222386");

        mvc.perform(MockMvcRequestBuilders
                        .post("/transactionId/gerar/folha_ib"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("2025022743671603934576222386"));

        verify(transactionIdService, times(1)).criaTransactionId();
    }

    @Test
    @DisplayName("Deve retornar InternalServerError quando ocorrer erro genérico")
    void deveRetornarInternalServerErrorQuandoOcorrerErroGenerico() throws Exception {
        doThrow(new ResponseStatusException(HttpStatusCode.valueOf(500)))
                .when(transactionIdService).criaTransactionId();

        mvc.perform(MockMvcRequestBuilders
                        .post("/transactionId/gerar/folha_ib"))
                .andExpect(status().isInternalServerError());

        verify(transactionIdService, times(1)).criaTransactionId();
    }

}