package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.CadastroAssociadoServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastroAssociadoServiceTest {

    @Mock
    private CadastroAssociadoServiceClient client;
    @InjectMocks
    private CadastroAssociadoService service;

    @Test
    @DisplayName("Deve consultar e retornar dados do associado")
    void deveConsultarERetornarDadosDoAssociado() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var consultarDadosAssociadoResponse = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro);

        when(client.consultarDadosAssociado(any(ConsultarDadosAssociado.class))).thenReturn(consultarDadosAssociadoResponse);

        var retornado = service.consultarDadosAssociado(cadastro.getCpf());

        assertThat(retornado).isNotNull();
        assertThat(retornado.getOutConsultarDadosAssociado()).isNotNull();
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos()).isNotNull().hasSize(1);
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getNroDocumento()).isEqualTo(cadastro.getCpf());
        assertThat(retornado).isEqualTo(consultarDadosAssociadoResponse);
        verify(client).consultarDadosAssociado(any(ConsultarDadosAssociado.class));
    }

    @Test
    @DisplayName("Deve lançar WebserviceException quando ocorrer erro ao consultar dados do associado")
    void deveLancarWebserviceExceptionQuandoOcorrerErroNaConsultaDeDadosAssociado() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var cpf = cadastro.getCpf();

        when(client.consultarDadosAssociado(any(ConsultarDadosAssociado.class))).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.consultarDadosAssociado(cpf))
                .isInstanceOf(WebserviceException.class)
                .hasMessageContaining("Erro ao acessar serviço CadastroAssociadoService");

        verify(client, times(1)).consultarDadosAssociado(any(ConsultarDadosAssociado.class));
    }

    @Test
    @DisplayName("Deve consultar e retornar Optional com o oid do associado")
    void deveConsultarERetornarOptionalComOIdDoAssociado() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var consultarDadosAssociadoResponse = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro);
        var oidPessoaEsperado = consultarDadosAssociadoResponse.getOutConsultarDadosAssociado().getElementos().getFirst().getOidPessoa();

        when(client.consultarDadosAssociado(any(ConsultarDadosAssociado.class))).thenReturn(consultarDadosAssociadoResponse);

        var retornado = service.consultarCadastroOidPessoa(cadastro);

        assertThat(retornado).isNotEmpty().contains(oidPessoaEsperado);
        verify(client, times(1)).consultarDadosAssociado(any(ConsultarDadosAssociado.class));
    }

    @Test
    @DisplayName("Deve consultar e retornar Optional vazio quando a busca não possuir oid do usuário")
    void deveConsultarERetornarOptionalVazio() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var consultarDadosAssociadoResponse = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseSemElementos();

        when(client.consultarDadosAssociado(any(ConsultarDadosAssociado.class))).thenReturn(consultarDadosAssociadoResponse);

        var retornado = service.consultarCadastroOidPessoa(cadastro);

        assertThat(retornado).isEmpty();
        verify(client, times(1)).consultarDadosAssociado(any(ConsultarDadosAssociado.class));
    }

    @Test
    @DisplayName("Deve consultar e retornar Optional vazio quando a busca falhar ")
    void deveRetornarOptionalVazioQuandpFalharNaConsulta() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();

        when(client.consultarDadosAssociado(any(ConsultarDadosAssociado.class))).thenThrow(RuntimeException.class);

        var retornado = service.consultarCadastroOidPessoa(cadastro);

        assertThat(retornado).isEmpty();
        verify(client, times(1)).consultarDadosAssociado(any(ConsultarDadosAssociado.class));
    }
}