package io.sicredi.aberturadecontaslegadooriginacao.service;

import io.sicredi.aberturadecontaslegadooriginacao.dto.DetalhesPedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.event.AcquisitionProductsCreationInputProducer;
import io.sicredi.aberturadecontaslegadooriginacao.event.AcquisitionProductsDetailsInputProducer;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessamentoProdutosServiceTest {

    public static final DetalhesPedidoDTO CANCELAR_PEDIDO_DTO = new DetalhesPedidoDTO("error", "123456","error descricao");
    public static final DetalhesPedidoDTO LIBERAR_PEDIDO_DTO = new DetalhesPedidoDTO(null, "123456",null);
    public static final OriginacaoLegado ORIGINACAO_LEGADO  = Instancio.of(OriginacaoLegado.class).create();
    public static final String TIPO_PRODUTO = "P1";
    public static final String LIBERADO = "LIBERADO";
    public static final String FALHA = "FALHA";
    public static final String PENDENTE = "PENDENTE";
    public static final String CANCELADO = "CANCELADO";
    public static final String FINALIZADO = "FINALIZADO";

    private ProcessamentoProdutosService produtosService;

    @Mock
    private AcquisitionProductsCreationInputProducer acquisitionProductsCreationInputProducer;
    @Mock
    private AcquisitionProductsDetailsInputProducer acquisitionProductsDetailsInputProducer;
    @Mock
    private MetricasService metricasService;
    @Mock
    private OriginacaoLegadoRepository repository;

    @BeforeEach
    void setUp() {
        this.produtosService = new ProcessamentoProdutosService(acquisitionProductsCreationInputProducer,
                acquisitionProductsDetailsInputProducer,
                metricasService, repository, Boolean.FALSE);
        ORIGINACAO_LEGADO.setStatus(PENDENTE);
        ORIGINACAO_LEGADO.setDetalheProduto(this.criarStubParaDetalheProduto());
    }

    @Test
    @DisplayName("Deve processar status pedido liberado")
    void deveProcessarStatusPedidoLiberado() {
        doNothing().when(metricasService).incrementCounter(any());
        doNothing().when(acquisitionProductsCreationInputProducer).send(any());
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));
        when(repository.save(any())).thenReturn(ORIGINACAO_LEGADO);
        var detalhesProduto = new HashMap<String, DetalheProduto>();
        detalhesProduto.put(TIPO_PRODUTO,Instancio.of(DetalheProduto.class).create());
        ORIGINACAO_LEGADO.setDetalheProduto(detalhesProduto);

        OriginacaoLegado resultado = this.produtosService.processarStatusPedido(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), LIBERADO, LIBERAR_PEDIDO_DTO);

        assertEquals(FINALIZADO, resultado.getStatus());
        verify(metricasService, times(1)).incrementCounter(any());
        verify(acquisitionProductsCreationInputProducer, times(1)).send(any());
    }

    @Test
    @DisplayName("Deve processar status pedido cancelado")
    void deveProcessarStatusPedidoCancelado() {
        doNothing().when(metricasService).incrementCounter(any());
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));
        when(repository.save(any())).thenReturn(ORIGINACAO_LEGADO);
        var detalhesProduto = new HashMap<String, DetalheProduto>();
        detalhesProduto.put(TIPO_PRODUTO,Instancio.of(DetalheProduto.class).create());
        ORIGINACAO_LEGADO.setDetalheProduto(detalhesProduto);

        OriginacaoLegado resultado = this.produtosService.processarStatusPedido(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), CANCELADO, LIBERAR_PEDIDO_DTO);

        assertEquals(PENDENTE, resultado.getStatus());
        verify(metricasService, times(1)).incrementCounter(any());
        verify(acquisitionProductsCreationInputProducer, times(0)).send(any());
    }

    @Test
    @DisplayName("Deve cancelar pedido manualmente")
    void deveCancelarPedidoManualmente() {
        doNothing().when(metricasService).incrementCounter(any());
        doNothing().when(acquisitionProductsDetailsInputProducer).send(any());
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));
        when(repository.save(any())).thenReturn(ORIGINACAO_LEGADO);

        OriginacaoLegado resultado = this.produtosService.cancelarItemPedidoManual(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), CANCELAR_PEDIDO_DTO);

        assertEquals(CANCELADO, resultado.getStatus());
        verify(metricasService, times(1)).incrementCounter(any());
        verify(acquisitionProductsDetailsInputProducer, times(1)).send(any());
    }

    @Test
    @DisplayName("Deve cancelar pedido automaticamente")
    void deveCancelarPedidoAutomaticamente() {
        ProcessamentoProdutosService processamentoProdutosService = new ProcessamentoProdutosService(
                acquisitionProductsCreationInputProducer,
                acquisitionProductsDetailsInputProducer,
                metricasService,
                repository,
                Boolean.TRUE);

        doNothing().when(metricasService).incrementCounter(any());
        doNothing().when(acquisitionProductsDetailsInputProducer).send(any());
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));
        when(repository.save(any())).thenReturn(ORIGINACAO_LEGADO);
        var detalhesProduto = new HashMap<String, DetalheProduto>();
        detalhesProduto.put(TIPO_PRODUTO,Instancio.of(DetalheProduto.class).create());
        ORIGINACAO_LEGADO.setDetalheProduto(detalhesProduto);

        OriginacaoLegado resultado = processamentoProdutosService.processarStatusPedido(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), CANCELADO, CANCELAR_PEDIDO_DTO);

        assertEquals(CANCELADO, resultado.getStatus());
        verify(metricasService, times(2)).incrementCounter(any());
        verify(acquisitionProductsDetailsInputProducer, times(1)).send(any());
    }

    @Test
    @DisplayName("Dado que status do pedido esteja concluído(Cancelado ou Finalizado) então não deve processar")
    void dadoQueStatusPedidoIgualAConcluidoNãoDeveProcessar() {
        ORIGINACAO_LEGADO.setStatus(FINALIZADO);
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));

        OriginacaoLegado resultado = this.produtosService.processarStatusPedido(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), LIBERADO, null);

        assertEquals(FINALIZADO, resultado.getStatus());
        verify(metricasService, times(0)).incrementCounter(any());
        verify(acquisitionProductsCreationInputProducer, times(0)).send(any());
    }

    @Test
    @DisplayName("Deve processar status pedido FALHA")
    void deveProcessarStatusPedidoFalha() {
        doNothing().when(metricasService).incrementCounter(any());
        when(repository.findByIdPedido(anyString())).thenReturn(Optional.of(ORIGINACAO_LEGADO));
        when(repository.save(any())).thenReturn(ORIGINACAO_LEGADO);
        var detalhesProduto = new HashMap<String, DetalheProduto>();
        var detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setTipoProduto(TIPO_PRODUTO);
        detalhesProduto.put(TIPO_PRODUTO, detalheProduto);
        ORIGINACAO_LEGADO.setDetalheProduto(detalhesProduto);

        OriginacaoLegado resultado = this.produtosService.processarStatusPedido(ORIGINACAO_LEGADO.getIdPedido(), ORIGINACAO_LEGADO.getDetalheProduto().get(TIPO_PRODUTO).getIdItemPedido(), FALHA, LIBERAR_PEDIDO_DTO);

        assertEquals(FALHA, resultado.getStatus());
        verify(metricasService, times(1)).incrementCounter(any());
        verify(acquisitionProductsCreationInputProducer, times(0)).send(any());
    }

    private Map<String, DetalheProduto> criarStubParaDetalheProduto() {
        return Map.of(
                TIPO_PRODUTO, DetalheProduto.builder().status("PROCESSANDO").idItemPedido("682f1a0209c0cf101e2421be").tipoProduto(TIPO_PRODUTO).build()
        );
    }

}