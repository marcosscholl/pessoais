package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.GetTelefones;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTelefonesBuilder extends GetTelefones {

    @Builder
    public GetTelefonesBuilder(long oidPessoa) {
        super();
        this.oidPessoa = oidPessoa;
    }
}
