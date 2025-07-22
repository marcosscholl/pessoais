package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class SOAPWebServiceRetryException extends RuntimeException {
    public SOAPWebServiceRetryException(String message) {
        super(message);
    }
}