package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class TransactionIDObrigatorioException extends RuntimeException {

    public TransactionIDObrigatorioException() {
        super("Cabeçalho 'TransactionId' é obrigatório.");
    }
}
