package io.sicredi.aberturadecontasalarioefetivador.utils;

import io.sicredi.aberturadecontasalarioefetivador.exceptions.LocalDateParaXMLGregorianException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

@Slf4j
@UtilityClass
public class DateUtils {

    private static final String STRING_ERRO_AO_CONVERTER_DATA_ATUAL = "Erro ao converter data atual.";

    public static XMLGregorianCalendar converterLocalDateParaXMLGregorian(final LocalDate data) {

        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                    data.getYear(),
                    data.getMonthValue(),
                    data.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED // Sem fuso horário
            );
        } catch (DatatypeConfigurationException e) {
            log.error(e.getMessage(), e.getCause());
            throw new LocalDateParaXMLGregorianException(STRING_ERRO_AO_CONVERTER_DATA_ATUAL, e);
        }
    }
}