package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadocontas.CadastroAssociadoContasClient;
import io.sicredi.aberturadecontasalarioefetivador.factories.CadastroAssociadoContasFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastroAssociadoContasServiceTest {

    @Mock
    private CadastroAssociadoContasClient client;
    @InjectMocks
    private CadastroAssociadoContasService service;
    private static final String COOPERATIVA = "0167";
    private static final String STATUS_CONTA = "ATIVA";
    private static final String CONTA_SALARIO = "CONTA_SALARIO";
    private static final String TITULAR = "TITULAR";
    private static final String DOCUMENTO = "00000000000";
    private static final String NUM_CONTA = "903677";
    private static final String STRING_ERRO = "Erro ao chamar o serviço externo";

    @Test
    @DisplayName("Deve retornar contas quando existirem")
    void deveRetornarContasQuandoExistirem() {
        when(client.buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false)));

        var result = service.buscarContasAssociado(DOCUMENTO, COOPERATIVA);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().conta()).isEqualTo(NUM_CONTA);
        verify(client, times(1)).buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma conta for encontrada")
    void deveRetornarListaVaziaQuandoNenhumaContaForEncontrada() {
        when(client.buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR))
                .thenReturn(Collections.emptyList());

        var retornado = service.buscarContasAssociado(DOCUMENTO, COOPERATIVA);

        assertThat(retornado).isNotNull().isEmpty();
        verify(client, times(1)).buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve retornar nulo quando Client retornar nulo")
    void deveRetornarNuloQuandoClientRetornarNulo() {
        when(client.buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR))
                .thenReturn(null);

        var retornado = service.buscarContasAssociado(DOCUMENTO, COOPERATIVA);

        assertThat(retornado).isNull();
        verify(client, times(1)).buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando ocorrer erro no Client")
    void deveRetornarListaVaziaQuandoOcorrerErroNoClient() {
        when(client.buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR))
                .thenThrow(new RuntimeException(STRING_ERRO));

        var retornado = service.buscarContasAssociado(DOCUMENTO, COOPERATIVA);

        assertThat(retornado).isEmpty();
        verify(client, times(1)).buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve invocar Client com parâmetros corretos")
    void deveInvocarClientComParametrosCorretos() {
        when(client.buscarContas(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        service.buscarContasAssociado(DOCUMENTO, COOPERATIVA);

        verify(client, times(1)).buscarContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve retornar lista com contas ativas do associado quando existirem")
    void deveRetornarListaComContasAtivasDoAssociadoQuandoExistirem() {
        when(client.buscarContasAtivas(DOCUMENTO, STATUS_CONTA))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(true)));

        var resultado = service.buscarContasAtivasAssociado(DOCUMENTO);

        assertThat(resultado).isNotNull().hasSize(1);
        assertThat(resultado.getFirst().originacao()).isEqualTo("DIGITAL");
        verify(client, times(1)).buscarContasAtivas(DOCUMENTO, STATUS_CONTA);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma conta ativa do associado for encontrada")
    void deveRetornarListaVaziaQuandoNenhumaContaAtivaDoAssociadoForEncontrada() {
        when(client.buscarContasAtivas(DOCUMENTO, STATUS_CONTA))
                .thenThrow(new RuntimeException(STRING_ERRO));

        var retornado = service.buscarContasAtivasAssociado(DOCUMENTO);

        assertThat(retornado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar true para associado digital")
    void deveRetornarTrueParaAssociadoDigital() {
        when(client.buscarContasAtivas(DOCUMENTO, STATUS_CONTA))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(true)));

        var resultado = service.isAssociadoDigital(DOCUMENTO);

        assertThat(resultado).isTrue();
        verify(client, times(1)).buscarContasAtivas(DOCUMENTO, STATUS_CONTA);
    }

    @Test
    @DisplayName("Deve retornar false para associado não digital")
    void deveRetornarFalseParaAssociadoNaoDigital() {
        when(client.buscarContasAtivas(DOCUMENTO, STATUS_CONTA))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false)));

        var resultado = service.isAssociadoDigital(DOCUMENTO);

        assertThat(resultado).isFalse();
        verify(client, times(1)).buscarContasAtivas(DOCUMENTO, STATUS_CONTA);
    }

    @Test
    @DisplayName("Deve buscar as contas salario por documento e retornar")
    void buscarContasSalarioPorDocumento() {
        when(client.buscarContasSalarioAssociado(DOCUMENTO, CONTA_SALARIO, TITULAR))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false)));

        var result = service.buscarContasSalarioAssociado(DOCUMENTO);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().conta()).isEqualTo(NUM_CONTA);
        verify(client, times(1)).buscarContasSalarioAssociado(DOCUMENTO, CONTA_SALARIO, TITULAR);
    }

    @Test
    @DisplayName("Deve buscar as contas salario por documento e retornar Vazio Quando ocorrer Erro")
    void buscarContasSalarioPorDocumentoERetornarVazioQuandoEmErro() {
        when(client.buscarContasSalarioAssociado(DOCUMENTO, CONTA_SALARIO, TITULAR))
                .thenThrow(new RuntimeException(STRING_ERRO));

        var retornado = service.buscarContasSalarioAssociado(DOCUMENTO);

        assertThat(retornado).isEmpty();
        verify(client, times(1)).buscarContasSalarioAssociado(DOCUMENTO, CONTA_SALARIO, TITULAR);
    }
}