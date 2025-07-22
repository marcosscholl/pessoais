package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.FontePagadora;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadoras;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadorasResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.dto.GetFontesPagadorasBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.dto.ListafontesPagadorasBuilder;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;

import java.util.List;

public class GetFontePagadoraFactory {

    public static GetFontesPagadoras getFontesPagadoras(Solicitacao solicitacao) {
        return GetFontesPagadorasBuilder.builder()
                .codigo(solicitacao.getCodConvenioFontePagadora())
                .cnpj(solicitacao.getCnpjFontePagadora())
                .build();
    }

    public static GetFontesPagadorasResponse getFontesPagadorasResponseValido(Solicitacao solicitacao) {
        GetFontesPagadorasResponse getFontesPagadorasResponse = new GetFontesPagadorasResponse();
        FontePagadora fontePagadora = new FontePagadora();
        fontePagadora.setCodigo(solicitacao.getCodConvenioFontePagadora());
        fontePagadora.setCnpj(solicitacao.getCnpjFontePagadora());
        fontePagadora.setAgencia(solicitacao.getNumCooperativa());
        fontePagadora.setNome("Fonte Pagadora XPTO");
        fontePagadora.setConta("801606");
        getFontesPagadorasResponse.setFontesPagadoras(
                ListafontesPagadorasBuilder.builder()
                        .fontePagadora(List.of(fontePagadora))
                        .build());
        return getFontesPagadorasResponse;
    }
    public static GetFontesPagadorasResponse getFontesPagadorasResponseInvalidoCodigoOuCnpj() {
        GetFontesPagadorasResponse getFontesPagadorasResponse = new GetFontesPagadorasResponse();
        getFontesPagadorasResponse.setFontesPagadoras(ListafontesPagadorasBuilder.builder().build());
        return getFontesPagadorasResponse;
    }

    public static GetFontesPagadorasResponse getFontesPagadorasResponseInvalidoCooperativa(Solicitacao solicitacao) {
        GetFontesPagadorasResponse fontesPagadorasResponseInvalido = getFontesPagadorasResponseValido(solicitacao);
        fontesPagadorasResponseInvalido.getFontesPagadoras()
                .getFontePagadora()
                .get(0)
                .setAgencia("0101");
        return fontesPagadorasResponseInvalido;
    }

}
