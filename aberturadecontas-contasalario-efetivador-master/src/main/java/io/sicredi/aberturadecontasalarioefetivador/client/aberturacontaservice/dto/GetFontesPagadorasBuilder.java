package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadoras;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetFontesPagadorasBuilder extends GetFontesPagadoras {

    @Builder
    public GetFontesPagadorasBuilder(String codigo, String nome, String cnpj) {
        super();
        this.setCodigo(codigo);
        this.setNome(nome);
        this.setCnpj(cnpj);
    }
}