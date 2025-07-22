package io.sicredi.aberturadecontasalarioefetivador.client.emailservice.dto;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailBuilder extends Email {
    @Builder
    public EmailBuilder(long oidPessoa, long userId, String userLogin, String branchCode, String email, long oidTabela, String tipo) {
        super();
        this.oidPessoa = oidPessoa;
        this.userId = userId;
        this.userLogin = userLogin;
        this.agencia = branchCode;
        this.email = email;
        this.oidTabela = oidTabela;
        this.tipo = tipo;
    }
}
