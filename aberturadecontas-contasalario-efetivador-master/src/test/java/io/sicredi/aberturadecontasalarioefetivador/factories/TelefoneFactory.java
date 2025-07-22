package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.*;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto.*;

import java.util.List;

public class TelefoneFactory {

    public static final String TELEFONE_TIPO_CELULAR = "4";
    public static final String USER_LOGIN = "app_api_cta_salario";
    public static final long USER_ID = 333262L;
    public static final String BRANCH_CODE = "ACA";
    public static final String DDD = "51";
    public static final long TELEFONE = 997649249L;


    public static GetTelefonesResponse consultarTelefoneResponseSemResultado(){
        return GetTelefoneResponseBuilder.builder()
                .listaTelefone(ListaTelefoneBuilder.builder().build())
                .build();

    }

    public static GetTelefonesResponse consultarTelefoneResponseComResultado(DadosAssociado associado){
        return GetTelefoneResponseBuilder.builder()
                .listaTelefone(
                        ListaTelefoneBuilder.builder()
                        .telefones(List.of(TelefoneBuilder.builder()
                                .cpf(associado.getNroDocumento())
                                .oidPessoa(associado.getOidPessoa())
                                .userId(USER_ID)
                                .userLogin(USER_LOGIN)
                                .branchCode(BRANCH_CODE)
                                .ddd(DDD)
                                .idTelefone(associado.getOidPessoa()-1)
                                .telefone(TELEFONE)
                                .tipo(TELEFONE_TIPO_CELULAR)
                                .build())
                        ).build()
                ).build();

    }

    public static SalvarTelefone salvarTelefone(DadosAssociado associado) {
        return SalvarTelefoneBuilder.builder()
                .telefone(TelefoneBuilder.builder()
                        .cpf(associado.getNroDocumento())
                        .oidPessoa(associado.getOidPessoa())
                        .userId(USER_ID)
                        .userLogin(USER_LOGIN)
                        .branchCode(BRANCH_CODE)
                        .ddd(DDD)
                        .telefone(TELEFONE)
                        .tipo(TELEFONE_TIPO_CELULAR)
                        .build())
                .build();
    }

    public static SalvarTelefoneResponse salvarTelefoneResponse(DadosAssociado associado){
        SalvarTelefoneResponse salvarTelefoneResponse = new SalvarTelefoneResponse();
        salvarTelefoneResponse.setTelefone(TelefoneBuilder.builder()
                .cpf(associado.getNroDocumento())
                .oidPessoa(associado.getOidPessoa())
                .userId(USER_ID)
                .userLogin(USER_LOGIN)
                .branchCode(BRANCH_CODE)
                .ddd(DDD)
                .idTelefone(associado.getOidPessoa()+1)
                .telefone(TELEFONE)
                .tipo(TELEFONE_TIPO_CELULAR)
                .build());
        return salvarTelefoneResponse;
    }

    public static SalvarTelefoneResponse atualizarTelefoneResponse(DadosAssociado associado){
        SalvarTelefoneResponse salvarTelefoneResponse = salvarTelefoneResponse(associado);
        salvarTelefoneResponse.getTelefone().setOidTabela(associado.getOidPessoa()-1);
        return salvarTelefoneResponse;
    }
}
