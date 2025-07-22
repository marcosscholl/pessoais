package io.sicredi.aberturadecontaslegadooriginacao.client;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtil;
import br.com.sicredi.mua.commons.business.server.ejb.ProximoDiaUtilFilter;
import io.sicredi.aberturadecontaslegadooriginacao.config.ConfiguracaoServiceSOAPConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.xml.transform.StringSource;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

@SpringBootTest(classes = ConfiguracaoServiceSOAPConfig.class)
@ActiveProfiles("test")
public class AdminServiceSOAPClientTest {

    private AdminServiceSOAPClient adminServiceSOAPClient;

    private MockWebServiceServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.commons.business.server.ejb");
        marshaller.afterPropertiesSet();

        adminServiceSOAPClient = new AdminServiceSOAPClient(marshaller, ".wsdl");

        mockServer = MockWebServiceServer.createServer(adminServiceSOAPClient);
    }

    @Test
    @DisplayName("Deve consultar o próximo dia útil no serviço de consulta de dia útil")
    void deveBuscarProximoDiaUtilSuccesso() throws Exception {
        var request = readFileToStringSource("getProximoDiaUtilRequest.xml");
        var response = readFileToStringSource("getProximoDiaUtilResponse.xml");

        mockServer.expect(payload(request)).andRespond(withPayload(response));

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();

        GregorianCalendar mockRequest = new GregorianCalendar();
        mockRequest.set(2025, Calendar.APRIL, 25);
        XMLGregorianCalendar diaUtilRequest = datatypeFactory.newXMLGregorianCalendarDate(
                mockRequest.get(Calendar.YEAR),
                mockRequest.get(Calendar.MONTH) + 1,
                mockRequest.get(Calendar.DAY_OF_MONTH),
                DatatypeConstants.FIELD_UNDEFINED
        );


        var diaRequest = new GetProximoDiaUtil();
        var proximoDiaUtilFilter = new ProximoDiaUtilFilter();
        proximoDiaUtilFilter.setUf("RS");
        proximoDiaUtilFilter.setNomCidade("PORTO ALEGRE");
        proximoDiaUtilFilter.setDtUtil(diaUtilRequest);
        diaRequest.setInGetProximoDiaUtil(proximoDiaUtilFilter);

        var result = adminServiceSOAPClient.getProximoDiaUtil(diaRequest);
        assertEquals("2025-04-25T00:00:00-03:00", result.getOutGetProximoDiaUtil().getDataUtil().toXMLFormat());
    }

    private static StringSource readFileToStringSource(String filename) {
        try {
            InputStream file = AdminServiceSOAPClientTest.class.getClassLoader().getResourceAsStream("__files/" + filename);
            assert file != null;
            String content = new String(file.readAllBytes(), StandardCharsets.UTF_8);

            return new StringSource(content);
        } catch (IOException e) {
            throw new InternalServerException(e);
        }
    }
}
