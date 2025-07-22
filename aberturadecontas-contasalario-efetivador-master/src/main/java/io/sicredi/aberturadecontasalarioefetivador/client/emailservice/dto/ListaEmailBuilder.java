package io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.Email;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.ListaEmail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ListaEmailBuilder extends ListaEmail {

    @Builder
    public ListaEmailBuilder(List<Email> emails) {
        super();
        this.email = emails;
    }
}
