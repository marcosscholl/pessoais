package io.sicredi.aberturadecontasalarioefetivador.exceptions;

public class AgenciaECooperativaNaoCorrespondentesException extends RuntimeException {
    public AgenciaECooperativaNaoCorrespondentesException() {
        super("Sem correspondência entre cooperativa e agência informados");
    }
}
