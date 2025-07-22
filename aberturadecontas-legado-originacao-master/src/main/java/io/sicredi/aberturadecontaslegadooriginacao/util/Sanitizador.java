package io.sicredi.aberturadecontaslegadooriginacao.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class Sanitizador {

    private Sanitizador(){}

    public static String sanitizar(String input) {
        if (StringUtils.isBlank(input)) return null;

        String comAcento = "ÀÈÌÒÙàèìòùÁÉÍÓÚáéíóúÃÕãõÂÊÎÔÛâêîôûÄËÏÖÜäëïöüŸÿÑñÅåÇç";
        String semAcento = "AEIOUaeiouAEIOUaeiouAOaoAEIOUaeiouAEIOUaeiouYyNnAaCc";

        StringBuilder resultado = new StringBuilder();

        for (char c : input.toCharArray()) {
            int index = comAcento.indexOf(c);
            if (index >= 0) {
                resultado.append(semAcento.charAt(index));
            } else {
                resultado.append(c);
            }
        }

        String caracteresEspeciais = "Ææ¢£¥ªº¿½¼¡±!#$%&'()*+-[]^_¨`<=>?@{|}/\\\\\"0123456789'";
        String regex = "[" + Pattern.quote(caracteresEspeciais) + "]";
        return resultado.toString().replaceAll(regex, "").trim();
    }

}