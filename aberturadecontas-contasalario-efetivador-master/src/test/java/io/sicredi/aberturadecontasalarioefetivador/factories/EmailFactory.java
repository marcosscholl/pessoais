package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.*;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.EmailBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.GetEmailsBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.ListaEmailBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.SalvarEmailBuilder;

import java.util.List;

public class EmailFactory {

    public static final String EMAIL_PESSOAL = "P";
    public static final String USER_LOGIN_SIAT = "app_siat_cta_salario";
    public static final long USER_ID_SIAT = 86909L;
    public static final String BRANCH_CODE = "ACA";
    public static final String EMAIL_NOVO = "novo_mail@mail.com";
    public static final String EMAIL_EXISTENTE = "atualizar_mail@mail.com";



    public static GetEmails consultarEmailRequest(DadosAssociado dadosAssociado){
        return GetEmailsBuilder.builder()
                .oidPessoa(dadosAssociado.getOidPessoa())
                .build();
    }

    public static GetEmailsResponse consultarEmailResponseSemEmail(){
        GetEmailsResponse response = new GetEmailsResponse();
         response.setListaEmail(new ListaEmail());
         return response;
    }

    public static GetEmailsResponse consultarEmailResponseComEmail(DadosAssociado associado, String email){
        GetEmailsResponse response = new GetEmailsResponse();
        response.setListaEmail(
                ListaEmailBuilder.builder()
                        .emails(List.of(EmailBuilder.builder()
                                .oidPessoa(associado.getOidPessoa())
                                .userId(USER_ID_SIAT)
                                .userLogin(USER_LOGIN_SIAT)
                                .branchCode(BRANCH_CODE)
                                .email(email)
                                .tipo(EMAIL_PESSOAL)
                                .build()))
                        .build());
        return response;
    }

    public static SalvarEmail salvarEmailNovo(DadosAssociado dadosAssociado){
        return SalvarEmailBuilder.builder()
                .email(EmailBuilder.builder()
                        .oidPessoa(dadosAssociado.getOidPessoa())
                        .userId(USER_ID_SIAT)
                        .userLogin(USER_LOGIN_SIAT)
                        .branchCode(BRANCH_CODE)
                        .email(EMAIL_NOVO)
                        .tipo(EMAIL_PESSOAL)
                        .build())
                .build();
    }

    public static SalvarEmailResponse salvarEmailNovoResponse(DadosAssociado dadosAssociado){
        return salvarEmailResponse(dadosAssociado, EMAIL_NOVO, dadosAssociado.getOidPessoa()+1);
    }

    public static SalvarEmail salvarEmailExistente(DadosAssociado dadosAssociado){
        return SalvarEmailBuilder.builder()
                .email(EmailBuilder.builder()
                        .oidTabela(dadosAssociado.getOidPessoa()-1)
                        .oidPessoa(dadosAssociado.getOidPessoa())
                        .userId(USER_ID_SIAT)
                        .userLogin(USER_LOGIN_SIAT)
                        .branchCode(BRANCH_CODE)
                        .email(EMAIL_EXISTENTE)
                        .tipo(EMAIL_PESSOAL)
                        .build())
                .build();
    }

    public static SalvarEmailResponse salvarEmailExistenteResponse(DadosAssociado dadosAssociado){
        return salvarEmailResponse(dadosAssociado, EMAIL_EXISTENTE, dadosAssociado.getOidPessoa()-1);
    }

    private static SalvarEmailResponse salvarEmailResponse(DadosAssociado dadosAssociado, String email, long oidTabela) {
        SalvarEmailResponse salvarEmailResponse = new SalvarEmailResponse();
        salvarEmailResponse.setEmail(EmailBuilder.builder()
                .oidPessoa(dadosAssociado.getOidPessoa())
                .userId(USER_ID_SIAT)
                .userLogin(USER_LOGIN_SIAT)
                .branchCode(BRANCH_CODE)
                .email(email)
                .oidTabela(oidTabela)
                .tipo(EMAIL_PESSOAL)
                .build());
        return salvarEmailResponse;
    }
}
