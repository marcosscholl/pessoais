package io.sicredi.aberturadecontaslegadooriginacao.controller;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.framework.web.spring.exception.BadRequestException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.OriginacaoFisitalLegadoHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DetalhesPedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import io.sicredi.aberturadecontaslegadooriginacao.service.OriginacaoLegadoService;
import io.sicredi.aberturadecontaslegadooriginacao.service.ProcessamentoProdutosService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OriginacaoLegadoControllerTest {

    @MockBean
    private OriginacaoFisitalLegadoHandler handler;

    @MockBean
    private ProcessamentoProdutosService processamentoProdutosService;

    @MockBean
    private OriginacaoLegadoService originacaoLegadoService;

    @Autowired
    private MockMvc mvc;
    private static final String ID_PEDIDO = "xpto";

    private static final String PATH_BASE = "/api/v1/originacao-legado/";

    @Test
    @DisplayName("Deve processar request de consulta dos dados do pedido")
    public void deveProcessarRequesBuscarOriginacaoLegadoPorIdPedido() throws Exception {
        var originacaoLegadoDTO = Instancio.of(OriginacaoLegadoDTO.class).create();

        when(handler.buscarOriginacaoLegadoPorIdPedido(ID_PEDIDO)).thenReturn(originacaoLegadoDTO);

        mvc.perform(MockMvcRequestBuilders
                        .get(PATH_BASE + ID_PEDIDO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect((status().isOk()));

        verify(handler).buscarOriginacaoLegadoPorIdPedido(ID_PEDIDO);
    }


    @Test
    @DisplayName("Deve processar status do produto")
    void deveProcessarStatusDoProduto() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "{idPedido}/item-pedido/{idItemPedido}/status/{status}",
                                "123", "4568", "LIBERADO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(processamentoProdutosService, times(1)).processarStatusPedido(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve cancelar status do produto")
    void deveCancelarStatusDoProduto() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "{idPedido}/item-pedido/{idItemPedido}/cancelar-pedido",
                                "9856", "3652")
                        .content(JsonUtils.objetoParaJson(new DetalhesPedidoDTO("menssagem", "123456","descricao")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(processamentoProdutosService, times(1)).cancelarItemPedidoManual(any(), any(), any());
    }

    @Test
    @DisplayName("Deve mudar o status da originação para FALHA")
    void deveMudarStatusParaFalha() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "{idPedido}/item-pedido/{idItemPedido}/status/{status}",
                                "123", "4568", "FALHA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(processamentoProdutosService, times(1)).processarStatusPedido(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve atualizar o dia do primeiro pagamento e retornar o dia do primeiro pagamento do capital.")
    void deveAtualizarDiaPrimeiroPagamentoERetornaProximoDiaUtil() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "{idPedido}/atualizar-dia-primeiro-pagamento",
                                "9856")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(originacaoLegadoService, times(1)).atualizarDiaPrimeiroPagamentoCapital(any());
    }

    @Test
    @DisplayName("Deve retornar erro quando for chamado o método para atualizar o proximo dia util.")
    void deveRetornarErroQuandoForChamadoMetodoParaAtualizarDiaProximoDiaUtil() throws Exception {
        when(originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString())).thenThrow(new BusinessException("Erro de negócio"));

        mvc.perform(MockMvcRequestBuilders
                        .post(PATH_BASE + "{idPedido}/atualizar-dia-primeiro-pagamento",
                                "9856")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }
}
