package io.sicredi.aberturadecontaslegadooriginacao.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.sicredi.aberturadecontaslegadooriginacao.exception.JsonParaObjetoException;
import io.sicredi.aberturadecontaslegadooriginacao.exception.ObjetoParaJsonException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        objectMapper.findAndRegisterModules();
    }

    public static <T> T jsonParaObjeto(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception exception) {
            log.error("Erro na conversão de json para objeto. {}", exception, exception);
            throw new JsonParaObjetoException(exception);
        }
    }

    public static String objetoParaJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception exception) {
            log.error("Erro na conversão de objeto para json. {}", exception, exception);
            throw new ObjetoParaJsonException(exception);
        }
    }
}