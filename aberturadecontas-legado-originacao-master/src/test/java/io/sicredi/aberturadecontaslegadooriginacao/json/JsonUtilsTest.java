package io.sicredi.aberturadecontaslegadooriginacao.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.exception.JsonParaObjetoException;
import io.sicredi.aberturadecontaslegadooriginacao.exception.ObjetoParaJsonException;
import io.sicredi.aberturadecontaslegadooriginacao.testdata.TestDataFactory;
import org.apache.commons.lang3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JsonUtilsTest {

    static class ClasseTeste {
        public String getValue() {
            throw new SerializationException("Erro ao serializar");
        }
    }

    @Test
    @DisplayName("Deve converter objeto para json")
    void deveConverterObjetoParaJson() throws JsonProcessingException {
        var acquisitionEngineManagerItemsDTO = TestDataFactory.acquisitionEngineManagerItemDTOSomenteCapitalLegacyStarted();
        var json = JsonUtils.objetoParaJson(acquisitionEngineManagerItemsDTO);

        var result = new ObjectMapper().readValue(json, Map.class);
        var expected = new ObjectMapper().convertValue(acquisitionEngineManagerItemsDTO, Map.class);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve lançar ObjetoParaJsonException quando ocorrer erro ao converter objeto em json")
    void deveLancarObjetoParaJsonException() {
        var failingObject = new ClasseTeste();
        assertThatThrownBy(() -> JsonUtils.objetoParaJson(failingObject))
                .isInstanceOf(ObjetoParaJsonException.class);
    }

    @Test
    @DisplayName("Deve lançar JsonParaObjeto quando ocorrer erro ao converter json em objeto")
    void deveLancarJsonParaObjetoException() {
        assertThatThrownBy(() -> JsonUtils.jsonParaObjeto("{{{{}}}", AcquisitionEngineManagerItemsEventDTO.class))
                .isInstanceOf(JsonParaObjetoException.class);
    }

}