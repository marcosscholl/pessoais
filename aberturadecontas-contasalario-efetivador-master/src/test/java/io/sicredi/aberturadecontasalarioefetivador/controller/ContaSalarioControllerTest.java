package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.*;
import io.sicredi.aberturadecontasalarioefetivador.service.ConsultarContaSalarioService;
import io.sicredi.aberturadecontasalarioefetivador.service.HeaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaSalarioControllerTest {

    public static final String DOCUMENTO = "36185900084";
    public static final String CODIGO_CONVENIO = "3AO";
    public static final String CONSULTA_CONTA_SALARIO_DOCUMENTO_CONVENIO_URL_PATH = "/conta-salario/documento/36185900084/3AO";
    @MockBean
    private ConsultarContaSalarioService consultarContaSalarioService;
    @MockBean
    HeaderService headerService;
    @Autowired
    private MockMvc mvc;

    private static final String CABECALHO_EXTERNO_CANAL = "Canal";
    private static final String CANAL = "MEU_CANAL";

    @Test
    @DisplayName("Deve Consultar Documento Conta Salario e Retornar na lista quando encontrar")
    void deveConsultarContaSalarioDocumentoContaSalarioERetornarNaListaQuandoEncontrar() throws Exception {
        List<ConsultarContaSalarioResponseDTO> resultadoConsultaContaSalario = ConsultarContaSalarioResponseDTOFactory.consultarContaSalarioResponseDTO();

        doNothing().when(headerService).validarHeaderSolicitacao(anyString(), anyString());
        when(consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL)).thenReturn(resultadoConsultaContaSalario);

        mvc.perform(MockMvcRequestBuilders.get(CONSULTA_CONTA_SALARIO_DOCUMENTO_CONVENIO_URL_PATH)
                                .header(CABECALHO_EXTERNO_CANAL, CANAL)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("VALOR DEFAULT MOCK")))
                .andExpect(jsonPath("$[0].convenio.codigo", is(CODIGO_CONVENIO)))
                .andExpect(jsonPath("$[0].alteracoes", hasSize(3)));

        verifyNoInteractions(headerService);
        verify(consultarContaSalarioService).consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);
    }

    @Test
    @DisplayName("Deve consultar documento Conta Salario e Retornar lista vazia quando não encontrar")
    void deveConsultarContaSalarioDocumentoContaSalarioERetornarListaVaziaQuandoNaoEncontrar() throws Exception {
        when(consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL))
                .thenReturn(List.of());

        mvc.perform(MockMvcRequestBuilders.get(CONSULTA_CONTA_SALARIO_DOCUMENTO_CONVENIO_URL_PATH)
                .header(CABECALHO_EXTERNO_CANAL, CANAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verifyNoInteractions(headerService);
        verify(consultarContaSalarioService, times(1)).consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);
    }

    @Test
    @DisplayName("Deve retornar critica CPF Invalido")
    public void deveRetornarCriticaCPFInvalido() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/conta-salario/documento/03104322008/" + CODIGO_CONVENIO)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O campo 'documento' deve conter um CPF válido"));
    }

    @Test
    @DisplayName("Deve retornar crítica tamanho Codigo Fonte")
    public void deveRetornarCriticaTamanhoodigoFonte() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/conta-salario/documento/"+DOCUMENTO+"/" + DOCUMENTO)
                        .header(CABECALHO_EXTERNO_CANAL, CANAL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O campo 'convenio' deve ter no máximo 7 caracteres"));
    }

}