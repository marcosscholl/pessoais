package io.sicredi.aberturadecontasalarioefetivador.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoResponseDTOFactory;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class UtilsTest {

    @Test
    @DisplayName("Deve converter objeto para json")
    void deveConverterObjetoParaJson() throws JsonProcessingException, JSONException {
        var solicitacaoResponseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTODoisCadastros();
        String jsonEsperado = new ObjectMapper().writeValueAsString(solicitacaoResponseDTO);

        var resultado = Utils.printJson(solicitacaoResponseDTO);

        assertEquals(jsonEsperado, resultado, true);
    }
}