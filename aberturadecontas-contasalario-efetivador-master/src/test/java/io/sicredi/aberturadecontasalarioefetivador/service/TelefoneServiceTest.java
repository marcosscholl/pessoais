package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.GetTelefones;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.SalvarTelefone;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.TelefoneServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.TelefoneFactory;
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
class TelefoneServiceTest {
    @Mock
    private TelefoneServiceClient client;
    @InjectMocks
    private TelefoneService service;
    private static final String STRING_ERRO_CLIENT_TELEFONE = "Erro ao acessar serviço TelefoneService";
    private static final String BRANCH_CODE = "ACA";
    private static final String TELEFONE = "51997649249";

    @Test
    @DisplayName("Deve consultar telefones e retornar quando encontrar")
    void deveConsultarTelefonesERetornarQuandoEncontrar() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.consultarTelefones(any(GetTelefones.class)))
                .thenReturn(TelefoneFactory.consultarTelefoneResponseComResultado(dadosAssociado));

        var retornado = service.consultarTelefones(dadosAssociado);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaTelefone()).isNotNull();
        assertThat(retornado.getListaTelefone().getTelefone()).isNotNull().hasSize(1);
        assertThat(retornado.getListaTelefone().getTelefone().getFirst().getOidTabela())
                .isEqualTo(dadosAssociado.getOidPessoa()-1);
        assertThat(retornado.getListaTelefone().getTelefone().getFirst().getTipo()).isEqualTo("4");

        verify(client, times(1)).consultarTelefones(any(GetTelefones.class));
    }

    @Test
    @DisplayName("Deve consultar telefones e retornar lista vazia quando não encontrar")
    void deveConsultarTelefonesERetornarListaVaziaQuandoNaoEncontrar() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.consultarTelefones(any(GetTelefones.class)))
                .thenReturn(TelefoneFactory.consultarTelefoneResponseSemResultado());

        var retornado = service.consultarTelefones(dadosAssociado);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaTelefone()).isNotNull();
        assertThat(retornado.getListaTelefone().getTelefone()).isEmpty();

        verify(client, times(1)).consultarTelefones(any(GetTelefones.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro no client ao consultar telefones")
    void deveLancarExceptionQuandoOcorrerErroNoClientAoConsultarTelefones() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.consultarTelefones(any(GetTelefones.class)))
                .thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.consultarTelefones(dadosAssociado))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_TELEFONE);

        verify(client, times(1)).consultarTelefones(any(GetTelefones.class));
    }

    @Test
    @DisplayName("Deve salvar novo telefone")
    void deveSalvarNovoTelefone() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarTelefone(any(SalvarTelefone.class)))
                .thenReturn(TelefoneFactory.salvarTelefoneResponse(dadosAssociado));

        var retornado = service.salvarNovoTelefone(dadosAssociado, BRANCH_CODE, TELEFONE);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getTelefone()).isNotNull();
        assertThat(retornado.getTelefone().getOidTabela()).isNotNull().isEqualTo(dadosAssociado.getOidPessoa()+1);

        verify(client, times(1)).salvarTelefone(any(SalvarTelefone.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro no client ao salvar novo telefone")
    void deveLancarExceptionQuandoOcorrerErroNoClientAoSalvarNovoTelefone() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarTelefone(any(SalvarTelefone.class)))
                .thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.salvarNovoTelefone(dadosAssociado, BRANCH_CODE, TELEFONE))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_TELEFONE);

        verify(client, times(1)).salvarTelefone(any(SalvarTelefone.class));
    }

    @Test
    @DisplayName("Deve atualizar telefone quando já existir")
    void deveAtualizarTelefoneQuandoJaExistir() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarTelefone(any(SalvarTelefone.class)))
                .thenReturn(TelefoneFactory.atualizarTelefoneResponse(dadosAssociado));

        var retornado = service.atualizarTelefone(dadosAssociado, BRANCH_CODE, "55"+TELEFONE,
                dadosAssociado.getOidPessoa()-1);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getTelefone()).isNotNull();
        assertThat(retornado.getTelefone().getOidTabela()).isNotNull().isEqualTo(dadosAssociado.getOidPessoa()-1);

        verify(client, times(1)).salvarTelefone(any(SalvarTelefone.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro no client ao atualizar telefone")
    void deveLancarExceptionQuandoOcorrerErroNoClientAoAtualizarTelefone() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        long oidPessoa = dadosAssociado.getOidPessoa()-1;

        when(client.salvarTelefone(any(SalvarTelefone.class)))
                .thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.atualizarTelefone(dadosAssociado, BRANCH_CODE,
                TELEFONE, oidPessoa))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_TELEFONE);

        verify(client, times(1)).salvarTelefone(any(SalvarTelefone.class));
    }
}