package io.sicredi.aberturadecontasalarioefetivador.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;


public class TestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);
    private static final String PATTERN = "dd/MM/yyyy";

    public static <T> T loadObject(String path, Class<T> valueType){
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(PATTERN)));
        mapper.registerModule(javaTimeModule);
        T mappedObject = null;
        try (FileReader fileReader = new FileReader(requireNonNull(systemClassLoader.getResource(path)).getFile(), UTF_8)){
            mappedObject = mapper.readValue(fileReader, valueType);
        } catch (Exception e) {
            LOGGER.error("Não foi possível carregar o json! path: {} error: \n ",path,e);
            Thread.currentThread().interrupt();
        }
        return mappedObject;
    }

    public static String objetoString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    public static ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(PATTERN)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(PATTERN)));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
}
