package io.sicredi.aberturadecontasalarioefetivador.client;

import io.sicredi.aberturadecontasalarioefetivador.client.contasalarioservice.ContaSalarioServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.CriarContaSalarioResponseCustomizadoDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.CriarContaSalarioFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.CriarContaSalarioResponseFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoRequestDTOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaSalarioServiceClientTest {

    private ContaSalarioServiceClient contaSalarioServiceClient;
    @Mock
    Jaxb2Marshaller marshaller;
    @Mock
    private WebServiceTemplate webServiceTemplate;

    @BeforeEach
    void setUp() {
        contaSalarioServiceClient = new ContaSalarioServiceClient(marshaller, marshaller, "URI", 10000, 2);
        contaSalarioServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve criar conta salário e retornar resultado com uso do DTO customizado")
    void deveChamarClientCriarContaSalarioERetornarResponseResultadoComUsoDoCustomizado() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalario = CriarContaSalarioFactory.toCriarContaSalario(cadastro, false);
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, 0);
        var contaSalarioResponse = criarContaSalarioResponse.getContaSalarioResponse();
        var responseCustomizado = CriarContaSalarioResponseCustomizadoDTO.builder()
                .codConvenioFontePagadora(contaSalarioResponse.getCodConvenioFontePagadora())
                .numCPF(contaSalarioResponse.getNumCPF())
                .numCooperativa(contaSalarioResponse.getNumCooperativa())
                .numAgencia(contaSalarioResponse.getNumAgencia())
                .numConta(contaSalarioResponse.getNumConta())
                .codStatus(contaSalarioResponse.getCodStatus())
                .desStatus(contaSalarioResponse.getDesStatus())
                .build();

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(responseCustomizado);

        var retornado = contaSalarioServiceClient.criarContaSalario(criarContaSalario);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodConvenioFontePagadora()).isEqualTo(solicitacao.getCodConvenioFontePagadora());
        assertThat(retornado.getContaSalarioResponse().getNumCPF()).isEqualTo(cadastro.getCpf());
        assertThat(retornado.getContaSalarioResponse().getNumCooperativa()).isEqualTo(solicitacao.getNumCooperativa());
    }

    @Test
    @DisplayName("Deve efetuar retry 2 vezes após receber erro OSB382500 e retornar response genérico")
    void deveEfetuarRetryEmCodStatusOSB382500ERetornarResponseGenericoAposDoisRetry() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalario = CriarContaSalarioFactory.toCriarContaSalario(cadastro, false);
        var criarContaSalarioResponseErro =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseDTOErroGenerico(solicitacao, 0);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class)))
                .thenReturn(criarContaSalarioResponseErro);

        var retornado = contaSalarioServiceClient.criarContaSalario(criarContaSalario);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isEqualTo("OSB-382500");
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isEqualTo(" - OSB SERVICE CALLOUT ACTION RECEIVED SOAP FAULT RESPONSE");
        assertThat(retornado.getContaSalarioResponse().getNumConta()).isNull();

        verify(webServiceTemplate, times(2)).marshalSendAndReceive(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("Deve efetuar retry 2 vezes após receber erro OSB382500 e conseguir na segunda tentativa")
    void deveriaTentarCriarContaSalarioDuasVezesQuandoCodStatusOSB382500() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalario = CriarContaSalarioFactory.toCriarContaSalario(cadastro, false);
        var criarContaSalarioResponseErro = CriarContaSalarioResponseFactory.criarContaSalarioResponseDTOErroGenerico(solicitacao, 0);
        var criarContaSalarioResponseSucesso = CriarContaSalarioResponseFactory.criarContaSalarioResponseDTOSucesso(solicitacao, 0);


        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class)))
                .thenReturn(criarContaSalarioResponseErro)
                .thenReturn(criarContaSalarioResponseSucesso);

        var retornado = contaSalarioServiceClient.criarContaSalario(criarContaSalario);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isEqualTo("000");
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isEqualTo(" - A CONTA SALARIO FOI CRIADA COM SUCESSO!");
        assertThat(retornado.getContaSalarioResponse().getNumConta()).isEqualTo("892824");

        verify(webServiceTemplate, times(2)).marshalSendAndReceive(any(String.class), any(Object.class));
    }
}