package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.factories.ContaSalarioCoreEventosFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocoreeventos.ContaSalarioCoreEventosClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioCoreEventosDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class ContaSalarioCoreEventosServiceTest {

    public static final String AGENCIA = "0167";
    public static final String CONTA = "903677";
    @Mock
    private ContaSalarioCoreEventosClient client;

    @InjectMocks
    private ContaSalarioCoreEventosService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deveria Buscar Eventos Conta Salario e retornar Quando Encontrar")
    void deveriaBuscarEventosContaSalarioERetornarQuandoEncontrar() {
        List<ContaSalarioCoreEventosDTO> eventos = ContaSalarioCoreEventosFactory.contaSalarioCoreEventos();
        when(client.buscarEventosContaSalario(AGENCIA, CONTA)).thenReturn(eventos);

        var result = service.buscarEventosContaSalario(AGENCIA, CONTA);
        assertFalse(result.isEmpty());
        assertThat(result).hasSize(7);
        assertThat(result.get(1).tipos().getFirst()).isEqualTo("ALTERACAO_PORTABILIDADE");

        verify(client).buscarEventosContaSalario(AGENCIA, CONTA);
    }

    @Test
    @DisplayName("Deveria Buscar Eventos Conta Salario e retornar quando exception")
    void deveriaBuscarEventosContaSalarioERetornarVazioQuandoOcorrerException() {
        when(client.buscarEventosContaSalario(AGENCIA, CONTA)).thenThrow(new RuntimeException());

        var result = service.buscarEventosContaSalario(AGENCIA, CONTA);
        assertThat(result).isEmpty();

        verify(client).buscarEventosContaSalario(AGENCIA, CONTA);
    }

    @Test
    @DisplayName("Deveria Buscar Eventos Conta Salario e retornar Mapeado pata Alteracao")
    void buscarEventosAlteracaoContaSalario_portabilidade() {
        List<ContaSalarioCoreEventosDTO> eventos = ContaSalarioCoreEventosFactory.contaSalarioCoreEventos();
        List<ConsultarContaSalarioResponseDTO.Alteracao> eventosAlteracao = ContaSalarioCoreEventosFactory.contaSalarioEventosAlteracao();

        when(client.buscarEventosContaSalario(AGENCIA, CONTA)).thenReturn(eventos);

        var retornado = service.buscarEventosAlteracaoContaSalario(AGENCIA, CONTA);
        assertThat(retornado).isNotEmpty();
        assertThat(retornado).hasSize(5);
        assertThat(retornado.getFirst().dadosAlterados()).isNotEmpty();
        assertThat(retornado.stream().filter(a -> "ALTERACAO_PORTABILIDADE".equals(a.tipo()))).isNotEmpty();
        assertThat(retornado.getFirst()).isEqualTo(eventosAlteracao.getFirst());
        assertThat(retornado.get(1)).isEqualTo(eventosAlteracao.get(1));
        assertThat(retornado.get(2)).isEqualTo(eventosAlteracao.get(2));
        assertThat(retornado.get(3)).isEqualTo(eventosAlteracao.get(3));
        assertThat(retornado.get(4)).isEqualTo(eventosAlteracao.get(4));

    }
}