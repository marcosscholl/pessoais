package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.ListaTelefone;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.Telefone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListaTelefoneBuilder extends ListaTelefone {

    @Builder
    public ListaTelefoneBuilder(List<Telefone> telefones) {
        super();
        this.telefone = telefones;
    }
}
