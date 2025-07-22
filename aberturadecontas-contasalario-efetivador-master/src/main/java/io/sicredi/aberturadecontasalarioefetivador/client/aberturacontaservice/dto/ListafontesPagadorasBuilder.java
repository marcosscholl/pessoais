package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.FontePagadora;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.ListafontesPagadoras;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ListafontesPagadorasBuilder extends ListafontesPagadoras {

    @Builder
    public ListafontesPagadorasBuilder(List<FontePagadora> fontePagadora) {
        super();
        this.fontePagadora = fontePagadora;
    }
}