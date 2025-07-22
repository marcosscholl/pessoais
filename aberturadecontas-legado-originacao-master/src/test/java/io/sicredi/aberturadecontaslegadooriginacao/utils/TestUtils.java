package io.sicredi.aberturadecontaslegadooriginacao.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtils {

    public static LocalDateTime dataHoraSemFuso(String dataComFusoHorario){
        String[] dataQubrada = dataComFusoHorario.split("-");
        String dataRemontada = dataQubrada[0]+"-"+dataQubrada[1]+"-"+dataQubrada[2];
        String dataFormatadaPadraoCorreto;
        if (dataRemontada.length() >= 23) {
            dataFormatadaPadraoCorreto = dataComFusoHorario.substring(0, 23);
        } else if (dataRemontada.length() == 22) {
            dataFormatadaPadraoCorreto = dataComFusoHorario.substring(0, 22) + "0";
        } else if (dataRemontada.length() == 21) {
            dataFormatadaPadraoCorreto = dataComFusoHorario.substring(0, 21) + "00";
        } else {
            dataFormatadaPadraoCorreto = dataComFusoHorario.substring(0, 19) + ".000";
        }
        return LocalDateTime.parse(dataFormatadaPadraoCorreto, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
}
