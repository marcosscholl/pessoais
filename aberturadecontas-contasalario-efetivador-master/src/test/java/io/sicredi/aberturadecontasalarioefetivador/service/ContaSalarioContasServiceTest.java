package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;
import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.ContaSalarioContasServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaSalarioContasServiceTest {

    private static final String AGENCIA = "0167";
    private static final String NUMERO_CONTA = "903677";

    @Mock
    private ContaSalarioContasServiceClient contaSalarioContasServiceClient;

    @InjectMocks
    private ContaSalarioContasService contaSalarioContasService;

    @Test
    @DisplayName("Deve consultar conta salário em contas e retornar quando encontrar")
    void deveConsultarContaSalarioERetornarQuandoEncontrar() {
        ConsultarSaldoContaSalarioResponse consultarSaldoContaSalarioResponse =
                ConsultarSaldoContaSalarioFactory.consultarSaldoContaSalarioResponse();

        when(contaSalarioContasServiceClient.consultarContaSalario(AGENCIA, NUMERO_CONTA))
                .thenReturn(consultarSaldoContaSalarioResponse);

        var retornado = contaSalarioContasService.consultarContaSalario(AGENCIA, NUMERO_CONTA);
        assertThat(retornado).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario()).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO()).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO().getNumeroContaSalario()).isEqualTo(NUMERO_CONTA);

        verify(contaSalarioContasServiceClient).consultarContaSalario(AGENCIA, NUMERO_CONTA);
    }

    @Test
    @DisplayName("Deve lançar exception quando ocorrer erro ao consultar conta")
    void deveLancarExceptionQuandoCorrerErroAoConsultarConta() {
        when(contaSalarioContasServiceClient.consultarContaSalario(AGENCIA, NUMERO_CONTA))
                .thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> contaSalarioContasService.consultarContaSalario(AGENCIA, NUMERO_CONTA))
                .isInstanceOf(WebserviceException.class)
                .hasMessageContaining("Erro ao acessar serviço ContaSalarioContasService");

        verify(contaSalarioContasServiceClient).consultarContaSalario(AGENCIA, NUMERO_CONTA);
    }
}