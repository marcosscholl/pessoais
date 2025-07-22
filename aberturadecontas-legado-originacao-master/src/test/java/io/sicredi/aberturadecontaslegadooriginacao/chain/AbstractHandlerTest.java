package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AbstractHandlerTest {

    private TestHandler testHandler;
    private TestHandler proximoHandler;


    @BeforeEach
    public void setUp() {
        testHandler = new TestHandler();
        proximoHandler = new TestHandler();
        testHandler.setProximo(proximoHandler);;
    }

    @Test
    @DisplayName("Deve realizar processamento do próximo handler do chain")
    public void deveProcessarComProximoHandler() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();

        OriginacaoLegado result = testHandler.processar(event, originacaoLegado);

        assertEquals(originacaoLegado, result);
    }

    @Test
    @DisplayName("Deve realizar processamento do sem próximo handler do chain")
    public void deveProcessarSemProximoHandler() {
        var originacaoLegado = new OriginacaoLegado("xpto");
        var event = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();

        testHandler.setProximo(null);

        OriginacaoLegado result = testHandler.processar(event, originacaoLegado);

        assertEquals(originacaoLegado, result);
    }

}
