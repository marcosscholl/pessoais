package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class CanalNaoEncontradoOuInatvoException extends RuntimeException {
    public CanalNaoEncontradoOuInatvoException() {
        super("'Canal' não encontrado ou inativo.");
    }
}
