package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConfiguracaoDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;

import java.util.ArrayList;

public class SolicitacaoRequestDTOFactory {


    public static SolicitacaoRequestDTO solicitacaoDoisCadastrosCompletos() {
        var cadastros = new ArrayList<CadastroRequestDTO>();
        cadastros.add(CadastroRequestDTOFactory.cadastroCompleto("21180506073"));
        cadastros.add(CadastroRequestDTOFactory.cadastroCompleto("74077944058"));
        return SolicitacaoRequestDTO.builder()
                .numCooperativa("0101")
                .numAgencia("17")
                .codConvenioFontePagadora("3AO")
                .cnpjFontePagadora("18523110000101")
                .cadastros(cadastros)
                .configuracao(configuracao())
                .build();
    }

    private static ConfiguracaoDTO configuracao() {
        return ConfiguracaoDTO.builder()
                .urlWebhook("http://localhost/webhook")
                .portaHttp("8080")
                .build();
    }
}
