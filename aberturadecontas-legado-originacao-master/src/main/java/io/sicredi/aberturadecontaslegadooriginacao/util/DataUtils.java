package io.sicredi.aberturadecontaslegadooriginacao.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

public class DataUtils {

    private DataUtils() {
    }

    public static LocalDate converterStringToLocalDate(final String data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(data, formatter);
    }

    public static XMLGregorianCalendar converterLocalDateToXMLGregorianCalendar(final LocalDate dataAgendamento) {
        try {
            ZonedDateTime zonedDateTime = dataAgendamento.atStartOfDay(ZoneId.systemDefault());
            GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);

            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException ex) {
            return null;
        }
    }

    public static LocalDate converterXMLGregorianCalendarToLocalDate(final XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        GregorianCalendar gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();

        return gregorianCalendar.toZonedDateTime().toLocalDate();
    }

    public static String converterLocalDateParaString(ChronoLocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return data.format(formatter);
    }

}
