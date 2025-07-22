package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class TransactionIDComDataInvalidaException extends RuntimeException {

    public TransactionIDComDataInvalidaException(Throwable cause) {
        super("Cabeçalho 'TransactionId' com data inválida.", cause);
    }
}
