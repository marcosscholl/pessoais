package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionCheckingAccountClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.NumeroContaDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NumeroContaServiceTest {

    @InjectMocks
    private NumeroContaService numeroContaService;
    @Mock
    private AcquisitionCheckingAccountClient acquisitionCheckingAccountClient;
    private static final String ACCOUNT_LEGACY = "ACCOUNT_LEGACY";

    @Test
    @DisplayName("Deve recuperar o número da conta corrente quando possui oo produto de conta corrente com sucesso.")
    public void deveRecuperarNumeroDeContaComSucesso() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var detalhesProdutos = new HashMap<String, DetalheProduto>(0);
        var detalheProdutoContaCorrente = Instancio.of(DetalheProduto.class).create();
        detalheProdutoContaCorrente.setTipoProduto(ACCOUNT_LEGACY);
        detalhesProdutos.put(ACCOUNT_LEGACY, detalheProdutoContaCorrente);
        originacaoLegado.setDetalheProduto(detalhesProdutos);

        var numeroConta = numeroContaService.obterNumeroConta(originacaoLegado);

        assertEquals(numeroConta, originacaoLegado.getDetalheProduto().get(ACCOUNT_LEGACY).getNumeroConta());
        verify(acquisitionCheckingAccountClient, times(0)).buscarNumeroConta(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve recuperar o número da conta do serviço checking-account com sucesso.")
    public void deveRecuperarNumeroDeContaDoCheckingAccountComSucesso() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var numeroContaMock = "123456";

        when(acquisitionCheckingAccountClient.buscarNumeroConta(anyString(), anyString())).thenReturn(new NumeroContaDTO(numeroContaMock));

        var numeroConta = numeroContaService.obterNumeroConta(originacaoLegado);

        assertEquals(numeroConta, numeroContaMock);
        verify(acquisitionCheckingAccountClient, times(1)).buscarNumeroConta(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar null quando não tiver o produto conta corrente no pedido e retornar erro da chamado ao servico checking-account.")
    public void deveRetornarNullQuandoOcorrerErroNaChamadaDoServicoCheckingAccount() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        when(acquisitionCheckingAccountClient.buscarNumeroConta(anyString(), anyString())).thenThrow(BusinessException.class);

        var numeroConta = numeroContaService.obterNumeroConta(originacaoLegado);

        assertNull(numeroConta);
        verify(acquisitionCheckingAccountClient, times(1)).buscarNumeroConta(anyString(), anyString());
    }
}
