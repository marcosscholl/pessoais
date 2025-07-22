package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadoras;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadorasResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.AberturaContaServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.GetFontePagadoraFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoRequestDTOFactory;
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
class AberturaContaServiceTest {

    @Mock
    private AberturaContaServiceClient client;
    @InjectMocks
    private AberturaContaService service;

    @Test
    @DisplayName("Deve validar fonte pagadora quando código CNPJ e Coop são condizentes")
    void deveriaValidarFontePagadoraQuandoCodigoCnpjECoopCondizente() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var fontesPagadorasResponseValido = GetFontePagadoraFactory.getFontesPagadorasResponseValido(solicitacao);

        when(client.consultarFontesPagadoras(any(GetFontesPagadoras.class)))
                .thenReturn(fontesPagadorasResponseValido);

        var retornado = service.validarFontePagadora(solicitacao);

        assertThat(retornado).isTrue();
        verify(client, times(1)).consultarFontesPagadoras(any(GetFontesPagadoras.class));
    }

    @Test
    @DisplayName("Não deveria validar fonte pagadora quando código CNPJ e Coop não são correspondentes")
    void naoDeveriaValidarFontePagadoraQuandoCodigoOuCnpjSemCorrespondencia() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());

        GetFontesPagadorasResponse fontesPagadorasResponseSemCorresponcia = GetFontePagadoraFactory.getFontesPagadorasResponseInvalidoCodigoOuCnpj();

        when(client.consultarFontesPagadoras(any(GetFontesPagadoras.class))).thenReturn(fontesPagadorasResponseSemCorresponcia);

        boolean retornado = service.validarFontePagadora(solicitacao);

        assertThat(retornado).isFalse();
        verify(client, times(1)).consultarFontesPagadoras(any(GetFontesPagadoras.class));
    }

    @Test
    @DisplayName("Não deve validar fonte pagadora quando fonte pagadora for nula")
    void naoDeveValidarFontePagadoraQuandoFontePagadoraForNull() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());

        GetFontesPagadorasResponse fontesPagadorasResponseSemCorresponcia = GetFontePagadoraFactory.getFontesPagadorasResponseInvalidoCodigoOuCnpj();
        fontesPagadorasResponseSemCorresponcia.setFontesPagadoras(null);

        when(client.consultarFontesPagadoras(any(GetFontesPagadoras.class))).thenReturn(fontesPagadorasResponseSemCorresponcia);

        boolean retornado = service.validarFontePagadora(solicitacao);

        assertThat(retornado).isFalse();
        verify(client, times(1)).consultarFontesPagadoras(any(GetFontesPagadoras.class));
    }

    @Test
    @DisplayName("Deve lançar WebserviceException quando ocorrer erro ao consultar fonte pagadora")
    void deveLancarWebClientExceptionQuandoOcorrerErroAoConsultarFontePagadora() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());

        doThrow(new RuntimeException()).when(client).consultarFontesPagadoras(any(GetFontesPagadoras.class));

        assertThatThrownBy(() -> service.validarFontePagadora(solicitacao)).isInstanceOf(WebserviceException.class);

        verify(client, times(1)).consultarFontesPagadoras(any(GetFontesPagadoras.class));
    }
}