package io.sicredi.aberturadecontasalarioefetivador.utils;

import io.sicredi.aberturadecontasalarioefetivador.exceptions.LocalDateParaXMLGregorianException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DateUtilsTest {

    @Test
    @DisplayName("Deve converter LocalDate para XmlGregorianCalendar")
    void deveConverterLocalDateParaXmlGregorianCalendar() throws DatatypeConfigurationException {
        var localDate = LocalDate.of(2025, 6, 15);

        var xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                2025,
                6,
                15,
                DatatypeConstants.FIELD_UNDEFINED);

        XMLGregorianCalendar retorno = DateUtils.converterLocalDateParaXMLGregorian(localDate);

        assertThat(retorno).isEqualTo(xmlGregorianCalendar);
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao converter LocalDate em XmlGregorianCalendar")
    void deveLancarExceptionQuandoOcorrerErroAoConverterLocalDateEmXmlGregorianCalendar() {
        var localDate = LocalDate.of(2025, 6, 15);
        var datatypeFactoryMockedStatic = mockStatic(DatatypeFactory.class);

        datatypeFactoryMockedStatic.when(DatatypeFactory::newInstance)
                .thenThrow(DatatypeConfigurationException.class);

        assertThatThrownBy(() -> DateUtils.converterLocalDateParaXMLGregorian(localDate))
                .isInstanceOf(LocalDateParaXMLGregorianException.class)
                .message().contains("Erro ao converter data atual.");
    }

}