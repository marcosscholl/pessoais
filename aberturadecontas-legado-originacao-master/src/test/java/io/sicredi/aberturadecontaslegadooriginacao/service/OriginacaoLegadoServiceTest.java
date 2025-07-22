package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionCheckingAccountClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.NumeroContaDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OriginacaoLegadoServiceTest {

    @InjectMocks
    private OriginacaoLegadoService originacaoLegadoService;
    @Mock
    private OriginacaoLegadoRepository originacaoLegadoRepository;
    @Mock
    private ProximoDiaUtilService proximoDiaUtilService;
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";
    private final LocalDate dataFixa = LocalDate.of(2025, 6, 10);
    private final Clock clockFixo = Clock.fixed(dataFixa.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());


    @Test
    @DisplayName("Deve retornar erro quando não tem produto legado na originacao.")
    public void deveRetornarErroQuandoNaoTemProdutoLegadoNaOriginacao() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));

        assertThrows(BusinessException.class, () ->
            originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString())
        );

        verify(proximoDiaUtilService, times(0)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());
        verify(originacaoLegadoRepository, times(0)).save(any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve lancar erro quando não encontrar nenhum pedido na base de dados com o idPedido informado.")
    void deveLancarNotFoundExceptionQuandoPedidoNaoExiste() {
        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString())
        );
    }

    @Test
    @DisplayName("Deve lancar erro quando não tiver nenhum produto legado no pedido.")
    void deveLancarBussinesExceptionQuandoNaoTemProdutoLegadoNoPeddido() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));

        assertThrows(BusinessException.class, () ->
            originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString())
        );
    }

    @Test
    @DisplayName("Deve retornar o dia do primeiro pagamento do capita, quando o dia de primeiro pagamento do capital presente no pedido é maior que data atual.")
    void deveRetornarODiadoPrimeiroPagamentoSendoODiaAtualDoPedidoQuandoADataDoPrimeiroPagamentoEMaiorQueDataAtual() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var dataEsperada = LocalDate.of(2025, 6, 11);
        var detalhesProdutos = new HashMap<String, DetalheProduto>(0);
        var detalhe = Instancio.of(DetalheProduto.class).create();
        detalhe.getConfiguracao().getCapital().setDiaPagamento(dataEsperada);
        detalhe.setTipoProduto(CAPITAL_LEGACY);
        detalhesProdutos.put(CAPITAL_LEGACY, detalhe);
        originacaoLegado.setDetalheProduto(detalhesProdutos);

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));

        originacaoLegadoService = new OriginacaoLegadoService(originacaoLegadoRepository, clockFixo, proximoDiaUtilService);
        var diaPrimeiroPagamento = originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString());

        assertEquals(dataEsperada, diaPrimeiroPagamento);
    }

    @Test
    @DisplayName("Deve retornar o dia o próximo dia útil quando a data do primeiro pagamento do capital foi menor que a data  atual.")
    void deveRetornarProximoDiadoPrimeiroPagamentoQuandoDataPrimeiroPagamentoCapitalForMenorQueDataAtual() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var dataPrimeriPagamentoCapitalPedido = LocalDate.of(2025, 6, 9);
        var detalhesProdutos = new HashMap<String, DetalheProduto>(0);
        var detalhe = Instancio.of(DetalheProduto.class).create();
        detalhe.getConfiguracao().getCapital().setDiaPagamento(dataPrimeriPagamentoCapitalPedido);
        detalhe.setTipoProduto(CAPITAL_LEGACY);
        detalhesProdutos.put(CAPITAL_LEGACY, detalhe);
        originacaoLegado.setDetalheProduto(detalhesProdutos);

        when(originacaoLegadoRepository.findByIdPedido(anyString())).thenReturn(Optional.of(originacaoLegado));
        when(proximoDiaUtilService.obterPrimeiroDiaPagamento(anyString(),anyString(),anyString())).thenReturn(dataFixa);

        originacaoLegadoService = new OriginacaoLegadoService(originacaoLegadoRepository, clockFixo, proximoDiaUtilService);
        var diaPrimeiroPagamento = originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(anyString());

        assertEquals(originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento(), diaPrimeiroPagamento);
        verify(originacaoLegadoRepository, times(1)).save(any(OriginacaoLegado.class));
        verify(proximoDiaUtilService, times(1)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());
    }
}
