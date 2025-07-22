package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtil;
import br.com.sicredi.mua.commons.business.server.ejb.ProximoDiaUtilFilter;
import io.sicredi.aberturadecontaslegadooriginacao.client.AdminServiceSOAPClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import static io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProximoDiaUtilService {

    private final AdminServiceSOAPClient client;
    private final Clock clock;

    public LocalDate obterPrimeiroDiaPagamento(final String dataAgendadaPagamento, final String siglaEstado, final String nomeCidade) {
        log.info("Iniciando obtençao do próximo dia útil [{}], estado [{}], cidade [{}]", dataAgendadaPagamento, siglaEstado, nomeCidade);
        try {
            var dataAgendamento = converterStringToLocalDate(dataAgendadaPagamento);
            if (dataAgendamento.isBefore(LocalDate.now(clock))) {
                dataAgendamento = LocalDate.now(clock);
            }

            var proximoDiaUtil = obterDiaUtil(dataAgendamento, siglaEstado, nomeCidade);
            log.info("Proximo dia util recuperado com sucesso. Próximo dia útil [{}], estado [{}], cidade [{}]", proximoDiaUtil, siglaEstado, nomeCidade);

            return proximoDiaUtil;
        } catch (Exception ex) {
            throw new BusinessException("A data informada não está no padrão esperado [yyyy-MM-dd]");
        }
    }

    private LocalDate obterDiaUtil(final LocalDate data, final String siglaEstado, final String nomeCidade) {
        var dataConvertidaParaXMLGregorianCalendar = converterLocalDateToXMLGregorianCalendar(data);
        if (Objects.isNull(dataConvertidaParaXMLGregorianCalendar)) {
            return null;
        }
        var requestProximoDiaUtil = construirRequestProximoDiaUtil(dataConvertidaParaXMLGregorianCalendar, siglaEstado, nomeCidade);
        var proximoDiaUtilResponse = client.getProximoDiaUtil(requestProximoDiaUtil);
        return converterXMLGregorianCalendarToLocalDate(proximoDiaUtilResponse.getOutGetProximoDiaUtil().getDataUtil());
    }

    private GetProximoDiaUtil construirRequestProximoDiaUtil(final XMLGregorianCalendar diaUtil, final String siglaEstado, final String nomeCidade) {
        var diaUtilRequest = new GetProximoDiaUtil();
        var proximoDiaUtilFilter = new ProximoDiaUtilFilter();
        proximoDiaUtilFilter.setUf(siglaEstado);
        proximoDiaUtilFilter.setNomCidade(nomeCidade);
        proximoDiaUtilFilter.setDtUtil(diaUtil);
        diaUtilRequest.setInGetProximoDiaUtil(proximoDiaUtilFilter);
        return diaUtilRequest;
    }
}
