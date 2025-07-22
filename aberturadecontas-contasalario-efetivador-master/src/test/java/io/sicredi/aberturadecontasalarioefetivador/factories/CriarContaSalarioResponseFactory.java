package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.ContaSalarioResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.CriarContaSalarioResponseCustomizadoDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;

import java.util.Objects;

public class CriarContaSalarioResponseFactory {

    public static CriarContaSalarioResponse criarContaSalarioResponseSucesso(Solicitacao solicitacao, int cadastroIndex) {
        CriarContaSalarioResponseCustomizadoDTO responseCustomizado = criarContaSalarioResponseDTOSucesso(solicitacao, cadastroIndex);
        return toCriarContaSalarioResponse(responseCustomizado);
    }

    public static CriarContaSalarioResponseCustomizadoDTO criarContaSalarioResponseDTOSucesso(Solicitacao solicitacao, int cadastroIndex) {
        Cadastro cadastro = solicitacao.getCadastros().get(cadastroIndex);

        return criarContaSalarioResponseCustomizado(solicitacao, cadastro, "892824",
                        "000", " - A CONTA SALARIO FOI CRIADA COM SUCESSO!");
    }

    public static CriarContaSalarioResponse criarContaSalarioResponseSucessoComCritica(Solicitacao solicitacao, int cadastroIndex) {
        Cadastro cadastro = solicitacao.getCadastros().get(cadastroIndex);

        CriarContaSalarioResponseCustomizadoDTO responseCustomizado =
                criarContaSalarioResponseCustomizado(solicitacao, cadastro, "892824",
                        "000", "OS DADOS DO TITULAR ESTAO INCONSISTENTES COM OS DA BASE. - A CONTA SALARIO FOI CRIADA COM SUCESSO!");

        return toCriarContaSalarioResponse(responseCustomizado);
    }

    public static CriarContaSalarioResponse criarContaSalarioResponseErroGenerico(Solicitacao solicitacao, int cadastroIndex) {
        CriarContaSalarioResponseCustomizadoDTO responseCustomizado = criarContaSalarioResponseDTOErroGenerico(solicitacao, cadastroIndex);
        return toCriarContaSalarioResponse(responseCustomizado);
    }

    public static CriarContaSalarioResponseCustomizadoDTO criarContaSalarioResponseDTOErroGenerico(Solicitacao solicitacao, int cadastroIndex) {
        Cadastro cadastro = solicitacao.getCadastros().get(cadastroIndex);

        return criarContaSalarioResponseCustomizado(solicitacao, cadastro, null,
                "OSB-382500", " - OSB SERVICE CALLOUT ACTION RECEIVED SOAP FAULT RESPONSE");
    }

    private static CriarContaSalarioResponseCustomizadoDTO criarContaSalarioResponseCustomizado(Solicitacao solicitacao,
                                                                                                Cadastro cadastro, String numConta, String codStatus,
                                                                                                String desStatus) {
        return CriarContaSalarioResponseCustomizadoDTO.builder()
                .codConvenioFontePagadora(solicitacao.getCodConvenioFontePagadora())
                .numCPF(cadastro.getCpf())
                .numCooperativa(solicitacao.getNumCooperativa())
                .numAgencia(solicitacao.getNumAgencia())
                .numConta(numConta)
                .codStatus(codStatus)
                .desStatus(desStatus)
                .build();
    }

    private static CriarContaSalarioResponse toCriarContaSalarioResponse(CriarContaSalarioResponseCustomizadoDTO responseCustomizado) {
        ContaSalarioResponse contaSalarioResponse = new ContaSalarioResponse();
        contaSalarioResponse.setCodConvenioFontePagadora(responseCustomizado.getCodConvenioFontePagadora());
        contaSalarioResponse.setNumCPF(responseCustomizado.getNumCPF());
        contaSalarioResponse.setNumCooperativa(responseCustomizado.getNumCooperativa());
        contaSalarioResponse.setNumAgencia(responseCustomizado.getNumAgencia());
        contaSalarioResponse.setNumConta(Objects.nonNull(responseCustomizado.getNumConta()) ? responseCustomizado.getNumConta() : null);
        contaSalarioResponse.setCodStatus(responseCustomizado.getCodStatus());
        contaSalarioResponse.setDesStatus(responseCustomizado.getDesStatus());

        CriarContaSalarioResponse criarContaSalarioResponse = new CriarContaSalarioResponse();
        criarContaSalarioResponse.setContaSalarioResponse(contaSalarioResponse);

        return criarContaSalarioResponse;
    }

}
