package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.*;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.exception.ProcessamentoOriginacaoFisitalLegadoException;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OriginacaoFisitalLegadoChainTest {

    @Mock
    private AcquisitionOrdersHandler acquisitionOrdersHandler;

    @Mock
    private CapitalAccountAcquisitionHandler capitalAccountAcquisitionHandler;

    @Mock
    private AccountHandler accountHandler;

    @Mock
    private InvestimentHandler investimentHandler;

    @Mock
    private CustomerDataHandler customerDataHandler;

    @Mock
    private GestentHandler gestentHandler;

    @Mock
    private RegisterDataHandler registerDataHandler;

    @Mock
    private OriginacaoLegadoRepository originacaoLegadoRepository;

    @InjectMocks
    private OriginacaoFisitalLegadoChain originacaoFisitalLegadoChain;

    @Test
    @DisplayName("Deve processar originacao fisital com todos os dados da originação legado preenchidos com sucesso")
    void deveProcessarOriginacaoFisitalLegadoComOriginacaoLegadoCompletoComSucesso() {
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));
        when(acquisitionOrdersHandler.processar(any(AcquisitionEngineManagerItemsEventDTO.class), any(OriginacaoLegado.class))).thenReturn(new OriginacaoLegado());

        originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(event);

        verify(acquisitionOrdersHandler).setProximo(customerDataHandler);
        verify(customerDataHandler).setProximo(registerDataHandler);
        verify(registerDataHandler).setProximo(gestentHandler);
        verify(gestentHandler).setProximo(accountHandler);
        verify(accountHandler).setProximo(investimentHandler);
        verify(investimentHandler).setProximo(capitalAccountAcquisitionHandler);
        verify(acquisitionOrdersHandler, times(1)).processar(event, originacaoLegado);
    }

    @Test
    @DisplayName("Deve processar originacao fisital legado sem todos os dados da originação legado preenchidos com sucesso")
    void deveProcessarOriginacaoFisitalLegadoSemOriginacaoLegadoPreenchidoComSucesso() {
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(event.idPedido());

        when(originacaoLegadoRepository.findByIdPedido(event.idPedido())).thenReturn(Optional.of(originacaoLegado));
        when(acquisitionOrdersHandler.processar(any(AcquisitionEngineManagerItemsEventDTO.class), any(OriginacaoLegado.class))).thenReturn(new OriginacaoLegado());
        when(originacaoLegadoRepository.save(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);


        originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(event);

        verify(acquisitionOrdersHandler).setProximo(customerDataHandler);
        verify(customerDataHandler).setProximo(registerDataHandler);
        verify(registerDataHandler).setProximo(gestentHandler);
        verify(gestentHandler).setProximo(accountHandler);
        verify(accountHandler).setProximo(investimentHandler);
        verify(investimentHandler).setProximo(capitalAccountAcquisitionHandler);
        verify(acquisitionOrdersHandler, times(1)).processar(event, originacaoLegado);
        verify(originacaoLegadoRepository, times(1)).save(originacaoLegado);
    }

    @Test
    @DisplayName("Deve lançar ProcessamentoOriginacaoFisitalLegadoException quando ocorrer um erro")
    void deveLancarProcessamentoOriginacaoFisitalLegadoExceptionQuandoOcorrerErro() {
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(event.idPedido());

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));
        when(originacaoLegadoRepository.save(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);

        doThrow(new RuntimeException("Erro")).when(acquisitionOrdersHandler).processar(any(AcquisitionEngineManagerItemsEventDTO.class), any(OriginacaoLegado.class));

        assertThrows(ProcessamentoOriginacaoFisitalLegadoException.class, () ->
            originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(event)
        );

        verify(acquisitionOrdersHandler, times(1)).processar(eq(event), any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve ignorar o processamento originacao fisital legado quando não tem nenhum produto legado no pedido.")
    void deveIgnorarProcessamentoOriginacaoFisitalLegadoQuandoNaoTemNenhumProdutoLegadoNoPedido() {
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(event.idPedido());

        var mapDetalhesProdutoNaoLegado = new HashMap<String, DetalheProduto>(1);
        var detalheProdutoPlataforma = Instancio.of(DetalheProduto.class).create();
        var originacaoLegadoMockRetornoProcessamentoAcquition = new OriginacaoLegado(event.idPedido());
        mapDetalhesProdutoNaoLegado.put("INDIVIDUAL_CHECKING_ACCOUNT",detalheProdutoPlataforma);
        originacaoLegadoMockRetornoProcessamentoAcquition.setDetalheProduto(mapDetalhesProdutoNaoLegado);

        when(originacaoLegadoRepository.findByIdPedido(event.idPedido())).thenReturn(Optional.of(originacaoLegado));
        when(acquisitionOrdersHandler.processar(any(AcquisitionEngineManagerItemsEventDTO.class), any(OriginacaoLegado.class))).thenReturn(originacaoLegadoMockRetornoProcessamentoAcquition);
        when(originacaoLegadoRepository.save(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);
        doNothing().when(originacaoLegadoRepository).delete(any(OriginacaoLegado.class));

        originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(event);

        verify(acquisitionOrdersHandler).setProximo(customerDataHandler);
        verify(customerDataHandler).setProximo(registerDataHandler);
        verify(registerDataHandler).setProximo(gestentHandler);
        verify(gestentHandler).setProximo(accountHandler);
        verify(accountHandler).setProximo(investimentHandler);
        verify(investimentHandler).setProximo(capitalAccountAcquisitionHandler);
        verify(acquisitionOrdersHandler, times(1)).processar(event, originacaoLegado);
        verify(originacaoLegadoRepository, times(1)).save(originacaoLegado);
        verify(originacaoLegadoRepository, times(1)).delete(originacaoLegado);
    }

}
