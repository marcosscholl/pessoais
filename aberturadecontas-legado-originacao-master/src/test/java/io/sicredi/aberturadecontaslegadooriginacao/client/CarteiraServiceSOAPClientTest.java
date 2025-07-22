package io.sicredi.aberturadecontaslegadooriginacao.client;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import io.sicredi.aberturadecontaslegadooriginacao.config.ConfiguracaoServiceSOAPConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

@SpringBootTest(classes = ConfiguracaoServiceSOAPConfig.class)
@ActiveProfiles("test")
public class CarteiraServiceSOAPClientTest {

    private CarteiraServiceSOAPClient carteiraServiceSOAPClient;

    private MockWebServiceServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.crm.ws.v1.carteiraservice");
        marshaller.afterPropertiesSet();

        carteiraServiceSOAPClient = new CarteiraServiceSOAPClient(marshaller, ".wsdl");

        mockServer = MockWebServiceServer.createServer(carteiraServiceSOAPClient);
    }

    @Test
    @DisplayName("Deve consultar o código da carteira com sucesso.")
    void deveBuscarCodigoCarteiraSuccesso() throws Exception {
        var request = readFileToStringSource("consultarCodigoCarteiraRequest.xml");
        var response = readFileToStringSource("consultarCodigoCarteiraResponse.xml");

        mockServer.expect(payload(request)).andRespond(withPayload(response));

        var result = carteiraServiceSOAPClient.obterCodigoCarteira("55252");
        assertEquals("219", result.getCodigoCarteira());
    }

    private static StringSource readFileToStringSource(String filename) {
        try {
            InputStream file = CarteiraServiceSOAPClientTest.class.getClassLoader().getResourceAsStream("__files/" + filename);
            assert file != null;
            String content = new String(file.readAllBytes(), StandardCharsets.UTF_8);

            return new StringSource(content);
        } catch (IOException e) {
            throw new InternalServerException(e);
        }
    }
}
