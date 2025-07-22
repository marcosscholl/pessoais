package io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.Email;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.SalvarEmail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SalvarEmailBuilder extends SalvarEmail {

    @Builder
    public SalvarEmailBuilder(Email email) {
        super();
        this.email = email;
    }
}
