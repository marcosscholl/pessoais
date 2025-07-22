package io.sicredi.aberturadecontasalarioefetivador.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class TransactionIdService {

    private final Random random = new SecureRandom();

    public String criaTransactionId() {
        String dataFormatada = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return dataFormatada + "4367" + gerarCodigoAletorio();
    }

    private String gerarCodigoAletorio() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 16) {
            sb.append(this.random.nextInt(10));
        }
        return sb.toString();
    }
}
