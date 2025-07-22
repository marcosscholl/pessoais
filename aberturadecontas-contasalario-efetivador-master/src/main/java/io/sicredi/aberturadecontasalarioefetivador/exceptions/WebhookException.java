package io.sicredi.aberturadecontasalarioefetivador.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WebhookException extends RuntimeException {

    public WebhookException(String msg) {
        super(msg);
    }

    public WebhookException(String msg, Exception e) {
        super(msg, e);
    }

}
