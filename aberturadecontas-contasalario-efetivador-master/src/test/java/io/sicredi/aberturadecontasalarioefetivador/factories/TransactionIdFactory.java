package io.sicredi.aberturadecontasalarioefetivador.factories;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransactionIdFactory {

    public static String transactionIdValido(Long codigoCanal) {
        String dataFormatada = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String codigo = String.format("%04d", codigoCanal);
        return dataFormatada + codigo + gerarCodigoAletorio();
    }

    private static String gerarCodigoAletorio() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 16) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}