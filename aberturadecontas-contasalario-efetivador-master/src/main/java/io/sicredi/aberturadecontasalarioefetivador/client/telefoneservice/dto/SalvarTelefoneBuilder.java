package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.SalvarTelefone;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.Telefone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalvarTelefoneBuilder extends SalvarTelefone {

    @Builder
    public SalvarTelefoneBuilder(Telefone telefone) {
        super();
        this.telefone = telefone;
    }
}
