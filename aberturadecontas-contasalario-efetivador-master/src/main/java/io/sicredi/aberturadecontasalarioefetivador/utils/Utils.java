package io.sicredi.aberturadecontasalarioefetivador.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static String printJson(Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
