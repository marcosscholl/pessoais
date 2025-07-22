package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalario;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice.AberturaContaCoexistenciaServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.factories.AberturaContaCoexistenciaServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AberturaContaCoexistenciaServiceTest {

    @Mock
    private AberturaContaCoexistenciaServiceClient client;
    @InjectMocks
    private AberturaContaCoexistenciaService service;
    private static final String BRANCH_CODE = "0810";
    private static final String CONTA = "90367-7";
    private static final String COOPERATIVA = "0167";
    private static final String DOCUMENTO = "20643481400";
    private static final long OID_PESSOA = 123456789L;
    private static final String STRING_ERRO_NO_CLIENTE = "Erro no cliente";

    @Test
    @DisplayName("Deve consultar conta salario")
    void deveConsultarContaSalarioERetornarResponseQuandoSucesso() {
        var response = AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada();

        when(client.consultarContaSalario(any())).thenReturn(response);

        var actualResponse = service.consultarContaSalario(BRANCH_CODE, CONTA, COOPERATIVA, DOCUMENTO, OID_PESSOA);

        assertThat(actualResponse).isNotNull().isEqualTo(response);

        verify(client, times(1)).consultarContaSalario(any());
    }

    @Test
    @DisplayName("Deve consultar conta salario passando apenas coop e conta")
    void deveConsultarContaSalarioApenasContaeCoopERetornarResponseQuandoSucesso() {
        var response = AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada();

        when(client.consultarContaSalario(any())).thenReturn(response);

        var actualResponse = service.consultarContaSalario(CONTA, COOPERATIVA);

        assertThat(actualResponse).isNotNull().isEqualTo(response);

        verify(client, times(1)).consultarContaSalario(any());
    }

    @Test
    @DisplayName("Deve lançar Exception quando client retornar erro")
    void deveLancarExcecaoQuandoClienteFalhar() {
        when(client.consultarContaSalario(any())).thenThrow(new RuntimeException(STRING_ERRO_NO_CLIENTE));

        assertThatThrownBy(() -> service.consultarContaSalario(BRANCH_CODE, CONTA, COOPERATIVA, DOCUMENTO, OID_PESSOA))
                .isInstanceOf(RuntimeException.class)
                .message().isEqualTo(STRING_ERRO_NO_CLIENTE);

        verify(client, times(1)).consultarContaSalario(any());
    }

    @Test
    @DisplayName("Deve passar parâmetros corretos para o client")
    void devePassarParametrosCorretosParaCliente() {
        var response = AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada();

        when(client.consultarContaSalario(any(GetContaSalario.class))).thenReturn(response);

        service.consultarContaSalario(BRANCH_CODE, CONTA, COOPERATIVA, DOCUMENTO, OID_PESSOA);

        verify(client, times(1)).consultarContaSalario(any(GetContaSalario.class));
    }

    @Test
    @DisplayName("Deve consultar instituições financeiras autorizadas")
    void deveConsultarInstituicoesFinanceiraAutorizadas() {
        var response = AberturaContaCoexistenciaServiceFactory.criarGetInstituicaoFinanceiraResponse();

        when(client.consultarInstituicaoFinanceira()).thenReturn(response);

        var retornado = service.consultarInstituicoesFinanceiraAutorizadas();

        assertThat(retornado).isNotNull().hasSize(2);
        assertThat(retornado.getFirst().codigo()).isEqualTo("299");
        assertThat(retornado.getFirst().nomeBanco()).isEqualTo("BCO AFINZ S.A. - BM");
        verify(client, times(1)).consultarInstituicaoFinanceira();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando client falhar na consulta de instituições financeiras")
    void deveRetornarListaVaziaQuandoClienteFalharConsultaDeInstituicaoFinanceira() {
        when(client.consultarInstituicaoFinanceira()).thenThrow(new RuntimeException(STRING_ERRO_NO_CLIENTE));

        var retornado = service.consultarInstituicoesFinanceiraAutorizadas();
        assertThat(retornado).isEmpty();

        verify(client, times(1)).consultarInstituicaoFinanceira();
    }
}