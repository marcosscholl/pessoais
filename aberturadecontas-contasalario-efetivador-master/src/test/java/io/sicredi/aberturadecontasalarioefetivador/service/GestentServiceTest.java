package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.gestest.GestentConnectorApiClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.AgenciaECooperativaNaoCorrespondentesException;
import io.sicredi.aberturadecontasalarioefetivador.factories.GestentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class GestentServiceTest {

    private static final String MENSAGEM_DE_ERRO = "Erro de conexão";
    @Mock
    private GestentConnectorApiClient gestentConnectorApiClient;
    @InjectMocks
    private GestentService gestentService;
    private static final String CODIGO_TIPO_ENTIDADE = "AGENCIA";
    private static final String CODIGO_SITUACAO = "ATIVA";
    private static final String CODIGO_AGENCIA = "17";
    private static final String CODIGO_COOPERATIVA = "0167";

    @Test
    @DisplayName("Deve consultar e obter entidade Sicredi")
    void deveConsultarEObterEntidadeSicredi() {
        var gestentDTO = GestentFactory.consultarEntidadeResponse();

        when(gestentConnectorApiClient.getEntidadeSicredi(
                0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO))
                .thenReturn(gestentDTO);

        var response = gestentService.obterEntidadeSicredi(CODIGO_COOPERATIVA, CODIGO_AGENCIA);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertFalse(response.content().isEmpty());
        assertEquals(8088, response.content().getFirst().idEntidadeSicredi());
        assertEquals("BGQ", response.content().getFirst().codigoEntidade());

        verify(gestentConnectorApiClient, times(1))
                .getEntidadeSicredi(0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO);
    }

    @Test
    @DisplayName("Deve lançar exceção tratada quando ocorrer erro ao consultar entidade Sicredi")
    void deveLancarExcecaoTratadaQuandoOcorrerErroAoConsultarEntidadeSicredi() {
        when(gestentConnectorApiClient.getEntidadeSicredi(anyInt(), anyInt(), anyString(), anyString(), anyString(),
                anyString())).thenThrow(new RuntimeException(MENSAGEM_DE_ERRO));

        var exception = assertThrows(RuntimeException.class,
                () -> gestentService.obterEntidadeSicredi(CODIGO_COOPERATIVA, CODIGO_AGENCIA));

        assertEquals(MENSAGEM_DE_ERRO, exception.getMessage());

        verify(gestentConnectorApiClient, times(1))
                .getEntidadeSicredi(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve consultar entidade de coop e agência correspondentes")
    void deveConsultarEntidadeDeCoopEAgenciaCorrespondetes() {
        var gestentDTO = GestentFactory.consultarEntidadeResponse();

        when(gestentConnectorApiClient.getEntidadeSicredi(0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA,
                CODIGO_COOPERATIVA, CODIGO_SITUACAO)).thenReturn(gestentDTO);

        var response = gestentService
                .consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(CODIGO_COOPERATIVA, CODIGO_AGENCIA);

        assertNotNull(response);
        assertEquals("BGQ", response);

        verify(gestentConnectorApiClient, times(1))
                .getEntidadeSicredi(0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO);
    }

    @Test
    @DisplayName("Deve lançar exceção tratada quando ocorrer erro ao consultar entidade Sicredi de coop e agência correspondentes")
    void deveLancarExcecaoTratadaQuandoAConsultaDeEntidadeSicrediDeCoopEAgenciaCorrespondentesRetornarVazio() {
        when(gestentConnectorApiClient.getEntidadeSicredi(anyInt(), anyInt(), anyString(), anyString(), anyString(),
                anyString())).thenThrow(new RuntimeException(MENSAGEM_DE_ERRO));

        assertThrows(AgenciaECooperativaNaoCorrespondentesException.class,
                () -> gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(CODIGO_COOPERATIVA, CODIGO_AGENCIA));

        verify(gestentConnectorApiClient, times(1))
                .getEntidadeSicredi(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção tratada quando ocorrer erro ao consultar entidade Sicredi de coop e agência correspondentes")
    void deveLancarExcecaoTratadaQuandoOcorrerErroAoConsultarEntidadeSicrediDeCoopEAgenciaCorrespondentes() {
        var gestentDTO = GestentFactory.consultarEntidadeResponse();
        gestentDTO = gestentDTO.toBuilder().content(new ArrayList<>()).build();

        when(gestentConnectorApiClient.getEntidadeSicredi(
                0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO))
                .thenReturn(gestentDTO);

        assertThrows(AgenciaECooperativaNaoCorrespondentesException.class,
                () -> gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(CODIGO_COOPERATIVA, CODIGO_AGENCIA));

        verify(gestentConnectorApiClient, times(1))
                .getEntidadeSicredi(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }
}