package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class TransactionIDForaDePadraoException extends RuntimeException {
    public TransactionIDForaDePadraoException() {
        super("Cabeçalho 'TransactionId' está fora do padrão estabelecido.");
    }
}
