package io.sicredi.aberturadecontaslegadooriginacao.client;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.config.DisableDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.sicredi.aberturadecontaslegadooriginacao.utils.TestUtils.dataHoraSemFuso;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = CustomerDataClient.class)
@EnableFeignClients(clients = CustomerDataClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class CustomerDataClientTest {

    @Autowired
    private CustomerDataClient customerDataClient;

    private static final String ID_CADASTRO = "29d787e6-8ff4-42be-b229-390e5e56f326";
    private static final String PATH_CUSTOMER_DATA ="/customers/" + ID_CADASTRO;

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar dados do cliente ao realizar consulta no customer-data")
    void deveRetornarOsDadosDoClienteAoRealizarConsultaNoCustomerData() {
        stubFor(get(urlMatching(PATH_CUSTOMER_DATA))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("customerData/cadastroCompleto.json")));

        var customerDataDTO = customerDataClient.buscarDadosCliente(ID_CADASTRO);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String responseBody = serveEvent.getResponse().getBodyAsString();

        verify(1, getRequestedFor(urlMatching(PATH_CUSTOMER_DATA)));
        assertEquals(JsonPath.read(responseBody, "$.id"), customerDataDTO.id());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.changeDate")), customerDataDTO.dataAtualizacao());
        assertEquals(JsonPath.read(responseBody, "$.fromLegacy"), customerDataDTO.origemLegado());
        assertEquals(JsonPath.read(responseBody, "$.personCondition.civilCapacity"), customerDataDTO.condicaoPessoal().capacidadeCivil().name());
        assertEquals(JsonPath.read(responseBody, "$.personCondition.condition"), customerDataDTO.condicaoPessoal().condicao().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].addressType"), customerDataDTO.dadosPessoais().enderecos().getFirst().tipo().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].allowDelivery"), customerDataDTO.dadosPessoais().enderecos().getFirst().permiteCorrespondencia());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.personal.addresses[0].changeDate").toString()), customerDataDTO.dadosPessoais().enderecos().getFirst().dataAtualizacao());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].city"), customerDataDTO.dadosPessoais().enderecos().getFirst().cidade());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].countryCode"), customerDataDTO.dadosPessoais().enderecos().getFirst().codigoPais());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].countryDescription"), customerDataDTO.dadosPessoais().enderecos().getFirst().descricaoPais());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].id"), customerDataDTO.dadosPessoais().enderecos().getFirst().id());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].mainAddress"), customerDataDTO.dadosPessoais().enderecos().getFirst().enderecoPrincipal());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].neighborhood"), customerDataDTO.dadosPessoais().enderecos().getFirst().bairro());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].noNumber"), customerDataDTO.dadosPessoais().enderecos().getFirst().semNumero());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].number"), customerDataDTO.dadosPessoais().enderecos().getFirst().numero());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].postalCode"), customerDataDTO.dadosPessoais().enderecos().getFirst().cep());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.personal.addresses[0].registerDate").toString()), customerDataDTO.dadosPessoais().enderecos().getFirst().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].source"), customerDataDTO.dadosPessoais().enderecos().getFirst().origem());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].state"), customerDataDTO.dadosPessoais().enderecos().getFirst().estado());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].street"), customerDataDTO.dadosPessoais().enderecos().getFirst().logradouro());
        assertEquals(JsonPath.read(responseBody, "$.personal.addresses[0].streetType"), customerDataDTO.dadosPessoais().enderecos().getFirst().tipoLogradouro());
        assertEquals(JsonPath.read(responseBody, "$.personal.birthCity"), customerDataDTO.dadosPessoais().naturalidadeCidade());
        assertEquals(JsonPath.read(responseBody, "$.personal.birthCountry"), customerDataDTO.dadosPessoais().nacionalidade());
        assertEquals(LocalDate.parse(JsonPath.read(responseBody, "$.personal.birthDate").toString()), customerDataDTO.dadosPessoais().dataNascimento());
        assertEquals(JsonPath.read(responseBody, "$.personal.birthState"), customerDataDTO.dadosPessoais().naturalidadeEstado());
        assertEquals(JsonPath.read(responseBody, "$.personal.emails[0].id"), customerDataDTO.dadosPessoais().emails().getFirst().id());
        assertEquals(JsonPath.read(responseBody, "$.personal.emails[0].email"), customerDataDTO.dadosPessoais().emails().getFirst().email());
        assertEquals(JsonPath.read(responseBody, "$.personal.emails[0].order"), customerDataDTO.dadosPessoais().emails().getFirst().ordem());
        assertEquals(JsonPath.read(responseBody, "$.personal.emails[0].verified"), customerDataDTO.dadosPessoais().emails().getFirst().verificado());
        assertEquals(JsonPath.read(responseBody, "$.personal.gender"), customerDataDTO.dadosPessoais().genero().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.idCard.idNumber"), customerDataDTO.dadosPessoais().identificacao().documento());
        assertEquals(JsonPath.read(responseBody, "$.personal.idCard.idType"), customerDataDTO.dadosPessoais().identificacao().tipo());
        assertEquals(LocalDate.parse(JsonPath.read(responseBody, "$.personal.idCard.issueDate").toString()), customerDataDTO.dadosPessoais().identificacao().dataEmissao());
        assertEquals(JsonPath.read(responseBody, "$.personal.idCard.issuingEntity"), customerDataDTO.dadosPessoais().identificacao().orgaoEmissor());
        assertEquals(JsonPath.read(responseBody, "$.personal.idCard.issuingState"), customerDataDTO.dadosPessoais().identificacao().estadoEmissao());
        assertEquals(JsonPath.read(responseBody, "$.personal.idCard.source"), customerDataDTO.dadosPessoais().identificacao().origem());
        assertEquals(JsonPath.read(responseBody, "$.personal.mainContactChannel"), customerDataDTO.dadosPessoais().canalComunicacaoPreferencial().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.maritalStatus"), customerDataDTO.dadosPessoais().estadoCivil().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.name.first"), customerDataDTO.dadosPessoais().nomeCompleto().nome());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[0].id"), customerDataDTO.dadosPessoais().parentes().getFirst().id());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[0].name"), customerDataDTO.dadosPessoais().parentes().getFirst().nome());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[0].parentRole"), customerDataDTO.dadosPessoais().parentes().getFirst().tipo().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[1].id"), customerDataDTO.dadosPessoais().parentes().getLast().id());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[1].name"), customerDataDTO.dadosPessoais().parentes().getLast().nome());
        assertEquals(JsonPath.read(responseBody, "$.personal.parents[1].parentRole"), customerDataDTO.dadosPessoais().parentes().getLast().tipo().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].allowSms"), customerDataDTO.dadosPessoais().telefones().getFirst().permiteSms());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].countryCode"), customerDataDTO.dadosPessoais().telefones().getFirst().codigoPais());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].id"), customerDataDTO.dadosPessoais().telefones().getFirst().id());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].number"), customerDataDTO.dadosPessoais().telefones().getFirst().numero());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].phoneType"), customerDataDTO.dadosPessoais().telefones().getFirst().tipo().name());
        assertEquals(JsonPath.read(responseBody, "$.personal.phones[0].stateCode"), customerDataDTO.dadosPessoais().telefones().getFirst().ddd());
        assertEquals(JsonPath.read(responseBody, "$.personal.suid"), customerDataDTO.dadosPessoais().cpf());
        assertEquals(JsonPath.read(responseBody, "$.personal.taxResidence"), customerDataDTO.dadosPessoais().residenciaExterior());
        assertEquals(JsonPath.read(responseBody, "$.professional.incomeNotInformed"), customerDataDTO.dadosProfissionais().rendaNaoInformada());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.incomes[0].changeDate").toString()), customerDataDTO.dadosProfissionais().rendas().getFirst().dataAtualizacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.incomes[0].id"), customerDataDTO.dadosProfissionais().rendas().getFirst().id());
        assertEquals(Double.valueOf(JsonPath.read(responseBody, "$.professional.incomes[0].income").toString()), customerDataDTO.dadosProfissionais().rendas().getFirst().valor());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.incomes[0].registerDate").toString()), customerDataDTO.dadosProfissionais().rendas().getFirst().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.incomes[0].source"), customerDataDTO.dadosProfissionais().rendas().getFirst().tipo().name());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.incomes[1].changeDate").toString()), customerDataDTO.dadosProfissionais().rendas().getLast().dataAtualizacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.incomes[1].id"), customerDataDTO.dadosProfissionais().rendas().getLast().id());
        assertEquals(Double.valueOf(JsonPath.read(responseBody, "$.professional.incomes[1].income").toString()), customerDataDTO.dadosProfissionais().rendas().getLast().valor());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.incomes[1].registerDate").toString()), customerDataDTO.dadosProfissionais().rendas().getLast().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.incomes[1].source"), customerDataDTO.dadosProfissionais().rendas().getLast().tipo().name());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.occupation.changeDate").toString()), customerDataDTO.dadosProfissionais().ocupacao().dataAtualizacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.occupation.code"), customerDataDTO.dadosProfissionais().ocupacao().codigo());
        assertEquals(JsonPath.read(responseBody, "$.professional.occupation.description"), customerDataDTO.dadosProfissionais().ocupacao().descricao());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.professional.occupation.registerDate").toString()), customerDataDTO.dadosProfissionais().ocupacao().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$.professional.occupationCode"), customerDataDTO.dadosProfissionais().codigoOcupacao());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$.registerDate")), customerDataDTO.dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$.status"), customerDataDTO.status());
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no customer-data " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlMatching(PATH_CUSTOMER_DATA))
                .willReturn(aResponse().withStatus(408)));

        assertThatThrownBy(() -> customerDataClient.buscarDadosCliente(ID_CADASTRO))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(PATH_CUSTOMER_DATA)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no customer-data " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlMatching(PATH_CUSTOMER_DATA))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> customerDataClient.buscarDadosCliente(ID_CADASTRO))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(PATH_CUSTOMER_DATA)));
    }
}