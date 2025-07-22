package io.sicredi.aberturadecontasalarioefetivador.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebserviceException extends RuntimeException {
    private final String message;
    private final Exception ex;
}