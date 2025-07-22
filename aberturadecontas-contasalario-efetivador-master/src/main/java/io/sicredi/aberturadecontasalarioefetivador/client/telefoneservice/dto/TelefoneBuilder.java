package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.Telefone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelefoneBuilder extends Telefone {

    @Builder
    public TelefoneBuilder(String cpf, long oidPessoa, long userId, String userLogin, String branchCode, String ddd, long idTelefone, long telefone, String tipo) {
        super();
        this.cpfCnpj = cpf;
        this.oidPessoa = oidPessoa;
        this.userId = userId;
        this.userLogin = userLogin;
        this.agencia = branchCode;
        this.ddd = ddd;
        this.oidTabela = idTelefone;
        this.telefone = telefone;
        this.tipo = tipo;
    }

}
