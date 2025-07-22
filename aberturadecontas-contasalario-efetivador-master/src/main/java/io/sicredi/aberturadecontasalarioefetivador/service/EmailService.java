package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.GetEmailsResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.SalvarEmailResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.EmailServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.EmailBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.GetEmailsBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto.SalvarEmailBuilder;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private static final String EMAIL_PESSOAL = "P";
    private static final String USER_LOGIN = "APP_API_CTA_SALARIO";
    private static final long USER_ID = 333262L;
    private static final String STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE_PARA_O_CADASTRO = "Erro ao acessar serviço EmailService para o cadastro {}";
    private static final String STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE = "Erro ao acessar serviço EmailService: ";
    private final EmailServiceClient client;

    public GetEmailsResponse consultarEmail(DadosAssociado associado) {
        try {
            return client.consultarEmail(
                    GetEmailsBuilder.builder()
                    .oidPessoa(associado.getOidPessoa())
                    .build());
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE + e.getMessage(), e);
        }
    }

    public SalvarEmailResponse salvarEmailNovo(DadosAssociado associado, String email, String branchCode) {
        try {
            SalvarEmailBuilder salvarEmailNovo = SalvarEmailBuilder.builder()
                    .email(EmailBuilder.builder()
                            .oidPessoa(associado.getOidPessoa())
                            .userId(USER_ID)
                            .userLogin(USER_LOGIN)
                            .branchCode(branchCode)
                            .email(email)
                            .tipo(EMAIL_PESSOAL)
                            .build())
                    .build();
            salvarEmailNovo.getEmail().setOidTabela(null);
            return client.salvarEmail(salvarEmailNovo);
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE + e.getMessage(), e);
        }
    }

    public SalvarEmailResponse salvarEmailExistente(DadosAssociado associado, String email, long oidEmail, String branchCode) {
        try {
            return client.salvarEmail(
                    SalvarEmailBuilder.builder()
                            .email(EmailBuilder.builder()
                                    .oidPessoa(associado.getOidPessoa())
                                    .userId(USER_ID)
                                    .userLogin(USER_LOGIN)
                                    .branchCode(branchCode)
                                    .email(email)
                                    .oidTabela(oidEmail)
                                    .tipo(EMAIL_PESSOAL)
                                    .build())
                            .build());
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_EMAIL_SERVICE + e.getMessage(), e);
        }
    }
}
