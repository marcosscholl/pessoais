package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarInstituicaoFinanceiraResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.AberturaContaCoexistenciaServiceFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.AberturaContaCoexistenciaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstituicoesFinanceirasControllerTest {

    @MockBean
    private AberturaContaCoexistenciaService aberturaContaCoexistenciaService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve consultar instituições financeiras autorizadas")
    void deveriaConsultarInstituicoesFinanceirasAutorizadas() throws Exception {
        var listaInstituicoes = AberturaContaCoexistenciaServiceFactory.criarListaInstituicoesFinanceirasDTO();

        when(aberturaContaCoexistenciaService.consultarInstituicoesFinanceiraAutorizadas()).thenReturn(listaInstituicoes);

        mockMvc.perform(get("/instituicoes-financeiras")
                        .header("Canal", "FOLHA_IB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(listaInstituicoes.size())))
                .andExpect(jsonPath("$[*].codigo",
                        everyItem(in(listaInstituicoes.stream().map(ConsultarInstituicaoFinanceiraResponseDTO::codigo).toList()))))
                .andExpect(jsonPath("$[*].nomeBanco",
                        everyItem(in(listaInstituicoes.stream().map(ConsultarInstituicaoFinanceiraResponseDTO::nomeBanco).toList()))));

        verify(aberturaContaCoexistenciaService, times(1)).consultarInstituicoesFinanceiraAutorizadas();
    }
}