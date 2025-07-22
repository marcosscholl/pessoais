package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class CanalSemCorrespondenciaException extends RuntimeException {
    public CanalSemCorrespondenciaException() {
        super("'TransactionId' Sem correspondência com o 'Canal'.");
    }
}
