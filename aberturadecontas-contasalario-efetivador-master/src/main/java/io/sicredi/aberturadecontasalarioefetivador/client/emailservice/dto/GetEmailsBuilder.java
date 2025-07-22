package io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.GetEmails;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetEmailsBuilder extends GetEmails {
    @Builder
    public GetEmailsBuilder(Long oidPessoa) {
        super();
        this.oidPessoa = oidPessoa;
    }
}
