package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.GetTelefonesResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.ListaTelefone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTelefoneResponseBuilder extends GetTelefonesResponse {

    @Builder
    public GetTelefoneResponseBuilder(ListaTelefone listaTelefone) {
        super();
        this.listaTelefone = listaTelefone;
    }
}
