package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtil;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtilResponse;
import br.com.sicredi.mua.commons.business.server.ejb.OutProximoDiaUtil;

import io.sicredi.aberturadecontaslegadooriginacao.client.AdminServiceSOAPClient;
import io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProximoDiaUtilServiceTest {

    @InjectMocks
    private ProximoDiaUtilService proximoDiaUtilService;

    @Mock
    private AdminServiceSOAPClient adminServiceSOAPClient;

    private final LocalDate dataFixa = LocalDate.of(2025, 6, 10);
    private final Clock clockFixo = Clock.fixed(dataFixa.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private static final String SIGLA_ESTADO = "RS";
    private static final String NOME_CIDADE = "Porto Alegre";

    @Test
    @DisplayName("Deve retornar a data atual quando o dia do agendamento for um dia útil")
    public void deveRetornarDataAtualAgendamentoForMenorQueDataAtual() throws DatatypeConfigurationException {
        var dataAgendamento = DataUtils.converterLocalDateParaString(dataFixa);
        mockProximoDiaUtil(dataFixa);
        proximoDiaUtilService = new ProximoDiaUtilService(adminServiceSOAPClient, clockFixo);

        var dataRetornada = proximoDiaUtilService.obterPrimeiroDiaPagamento(dataAgendamento, SIGLA_ESTADO, NOME_CIDADE);

        assertEquals(dataFixa, dataRetornada);
    }

    @Test
    @DisplayName("Deve setar a data de consulta do próximo dia útil sendo a data atual, pois a data informada é menor que a data atual.")
    public void deveSetarDataConsultaProximoDiaUtilSendoDataAtualPoisDataInformadaEMenorQueDataAtual() throws DatatypeConfigurationException {
        var dataAgendamento = DataUtils.converterLocalDateParaString(LocalDate.of(2025, 6, 9));
        mockProximoDiaUtil(dataFixa);
        proximoDiaUtilService = new ProximoDiaUtilService(adminServiceSOAPClient, clockFixo);

        var dataRetornada = proximoDiaUtilService.obterPrimeiroDiaPagamento(dataAgendamento, SIGLA_ESTADO, NOME_CIDADE);

        assertEquals(dataFixa, dataRetornada);
    }

    @Test
    @DisplayName("Deve retornar erro quando chamar o serviço SOAP para recuperar o próximo dia útil")
    public void deveRetornarExceptionQuandoChamarServicoSOAPParaRecuperarProximoDiaUtil() {
        var dataAgendamento = DataUtils.converterLocalDateParaString(LocalDate.of(2025, 6, 9));
        when(adminServiceSOAPClient.getProximoDiaUtil(any(GetProximoDiaUtil.class))).thenThrow(BusinessException.class);
        proximoDiaUtilService = new ProximoDiaUtilService(adminServiceSOAPClient, clockFixo);

        assertThrows(BusinessException.class, () ->
                proximoDiaUtilService.obterPrimeiroDiaPagamento(dataAgendamento, SIGLA_ESTADO, NOME_CIDADE)
        );
    }

    private XMLGregorianCalendar toXMLGregorianCalendar(LocalDate date) throws DatatypeConfigurationException {
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }

    private void mockProximoDiaUtil(LocalDate data) throws DatatypeConfigurationException {
        XMLGregorianCalendar xmlGregorianCalendar = toXMLGregorianCalendar(data);
        var outputDiaUtil = new OutProximoDiaUtil();
        outputDiaUtil.setDataUtil(xmlGregorianCalendar);
        var proximoDiaUtilResponse = new GetProximoDiaUtilResponse();
        proximoDiaUtilResponse.setOutGetProximoDiaUtil(outputDiaUtil);
        when(adminServiceSOAPClient.getProximoDiaUtil(any(GetProximoDiaUtil.class))).thenReturn(proximoDiaUtilResponse);
    }
}
