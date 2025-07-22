package io.sicredi.aberturadecontaslegadooriginacao.controller;

import io.sicredi.aberturadecontaslegadooriginacao.chain.OriginacaoFisitalLegadoChain;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.OriginacaoFisitalLegadoHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import org.instancio.Instancio;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TroubleshootingControllerTest {

    @MockBean
    private OriginacaoFisitalLegadoChain chain;
    @MockBean
    private OriginacaoFisitalLegadoHandler handler;

    @Autowired
    private MockMvc mvc;
    private static final String ID_PEDIDO = "xpto";

    private static final String PATH_BASE = "/api/v1/troubleshooting/";

    @Test
    @DisplayName("Deve processar request de consulta dos dados do pedido retornando os dados da entidade originação legado")
    public void deveProcessarRequesBuscarOriginacaoLegadoPorIdPedido() throws Exception {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        when(handler.buscarDadosEntidadeOriginacaoLegadoPorIdPedido(ID_PEDIDO)).thenReturn(JsonUtils.objetoParaJson(originacaoLegado));

        mvc.perform(MockMvcRequestBuilders
                        .get(PATH_BASE + "/originacao-legado/" + ID_PEDIDO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect((status().isOk()));

        verify(handler).buscarDadosEntidadeOriginacaoLegadoPorIdPedido(ID_PEDIDO);
    }

    @Test
    @DisplayName("Deve processar request de consulta dos dados do pedido")
    public void deveProcessarRequesProcessarOriginacaoLegado() throws Exception {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var request = JsonUtils.objetoParaJson(acquisitionEngineManagerItemsEventDTO);
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "/originacao-legado").content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(chain).processaOriginacaoFisitalLegado(acquisitionEngineManagerItemsEventDTO);
    }

    @Test
    @DisplayName("Deve processar uma originação legado a partir do id do pedido")
    public void deveProcessarRequesReprocessamentoOriginacaoLegadoPorId() throws Exception {
        var acquisitionEngineManagerItemsEventDTO = new AcquisitionEngineManagerItemsEventDTO(null,ID_PEDIDO, null, null);
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "/originacao-legado/processar/" + ID_PEDIDO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(chain).processaOriginacaoFisitalLegado(acquisitionEngineManagerItemsEventDTO);
    }

}
