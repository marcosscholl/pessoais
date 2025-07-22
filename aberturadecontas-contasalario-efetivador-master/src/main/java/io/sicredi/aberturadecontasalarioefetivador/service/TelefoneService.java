package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.GetTelefonesResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.SalvarTelefoneResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.TelefoneServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto.GetTelefonesBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto.SalvarTelefoneBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto.TelefoneBuilder;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TelefoneService {

    private static final String TELEFONE_TIPO_CELULAR = "4";
    private static final String USER_LOGIN = "APP_API_CTA_SALARIO";
    private static final long USER_ID = 333262L;
    private static final String STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE = "Erro ao acessar serviço TelefoneService: ";
    private static final String STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE_PARA_O_CADASTRO = "Erro ao acessar serviço TelefoneService para o cadastro {}";
    private final TelefoneServiceClient client;

    public GetTelefonesResponse consultarTelefones(DadosAssociado associado) {
        try {
            return client.consultarTelefones(GetTelefonesBuilder.builder()
                    .oidPessoa(associado.getOidPessoa())
                    .build());
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE + e.getMessage(), e);
        }
    }

    public SalvarTelefoneResponse salvarNovoTelefone(DadosAssociado associado, String branchCode, String telefone) {
        try {
            SalvarTelefoneBuilder salvarTelefoneNovo = SalvarTelefoneBuilder.builder()
                    .telefone(TelefoneBuilder.builder()
                            .cpf(associado.getNroDocumento())
                            .oidPessoa(associado.getOidPessoa())
                            .userId(USER_ID)
                            .userLogin(USER_LOGIN)
                            .branchCode(branchCode)
                            .ddd(extraiNumeroTelefone(telefone).ddd)
                            .telefone(Long.parseLong(extraiNumeroTelefone(telefone).telefone))
                            .tipo(TELEFONE_TIPO_CELULAR)
                            .build())
                    .build();
            salvarTelefoneNovo.getTelefone().setOidTabela(null);
            return client.salvarTelefone(salvarTelefoneNovo);
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE + e.getMessage(), e);
        }
    }

    public SalvarTelefoneResponse atualizarTelefone(DadosAssociado associado, String branchCode, String telefone, long idTelefone) {
        try {
            return client.salvarTelefone(
                    SalvarTelefoneBuilder.builder()
                            .telefone(TelefoneBuilder.builder()
                                    .cpf(associado.getNroDocumento())
                                    .oidPessoa(associado.getOidPessoa())
                                    .userId(USER_ID)
                                    .userLogin(USER_LOGIN)
                                    .branchCode(branchCode)
                                    .ddd(extraiNumeroTelefone(telefone).ddd)
                                    .idTelefone(idTelefone)
                                    .telefone(Long.parseLong(extraiNumeroTelefone(telefone).telefone))
                                    .tipo(TELEFONE_TIPO_CELULAR)
                                    .build())
                            .build());
        } catch (Exception e) {
            log.error(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE_PARA_O_CADASTRO, associado.getNroDocumento(), e);
            throw new WebserviceException(STRING_ERRO_AO_ACESSAR_SERVICO_TELEFONE_SERVICE + e.getMessage(), e);
        }
    }

    private NumeroTelefone extraiNumeroTelefone(String numeroTelefone) {
        String numeroSanitizado = numeroTelefone.replaceAll("\\D", "");

        if (numeroSanitizado.startsWith("55")) {
            numeroSanitizado = numeroSanitizado.substring(2);
        }

        String ddd = "";
        String telefone = "";

        if (numeroSanitizado.length() == 11 || numeroSanitizado.length() == 10) {
            ddd = numeroSanitizado.substring(0, 2);
            telefone = numeroSanitizado.substring(2);
        }
        return new NumeroTelefone(ddd, telefone);
    }

    private record NumeroTelefone(String ddd, String telefone) {}
}
