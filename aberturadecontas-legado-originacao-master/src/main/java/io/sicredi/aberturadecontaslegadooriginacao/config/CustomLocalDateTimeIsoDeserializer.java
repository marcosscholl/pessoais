package io.sicredi.aberturadecontaslegadooriginacao.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeIsoDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dataComFusoHorario = jsonParser.getText();
        return LocalDateTime.parse(dataSemFusoHoario(dataComFusoHorario), formatter);
    }

    private String dataSemFusoHoario(String dataComFusoHorario){
        String[] dataQubrada = dataComFusoHorario.split("-");
        String data = dataQubrada[0]+"-"+dataQubrada[1]+"-"+dataQubrada[2];
        if (data.length() >= 23) {
            return dataComFusoHorario.substring(0, 23);
        } else if (data.length() == 22) {
            return dataComFusoHorario.substring(0, 22) + "0";
        } else if (data.length() == 21) {
            return dataComFusoHorario.substring(0, 21) + "00";
        } else {
            return dataComFusoHorario.substring(0, 19) + ".000";
        }
    }
}

