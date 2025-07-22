package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoCritica;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DltEventService {
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String TRANSACTION_ID = "transactionId";
    private final CadastroRepository cadastroRepository;

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarErroSolicitacaoBureauRF(IdempotentAsyncRequest<Cadastro> cadastroRequest) {
        return processaErroSolicitacao(cadastroRequest, Critica.builder()
                .codigo("RFB007")
                .descricao("Receita Federal indisponível.")
                .tipo(TipoCritica.BLOQUEANTE).build());
    }

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarErroSolicitacaoCadastro(IdempotentAsyncRequest<Cadastro> cadastroRequest) {
        return processaErroSolicitacao(cadastroRequest, Critica.builder()
                .codigo("CCS001")
                .descricao("Erro no cadastro de conta salário.")
                .tipo(TipoCritica.BLOQUEANTE).build());
    }

    private IdempotentResponse<Cadastro> processaErroSolicitacao(IdempotentAsyncRequest<Cadastro> cadastroRequest, Critica critica) {
        var cadastro = cadastroRepository
                .findById(cadastroRequest.getValue().getId()).orElseThrow(NotFoundException::new);

        var transactionIdSolicitacao = cadastroRequest.getHeaders().get(TRANSACTION_ID_SOLICITACAO);
        var transactionId = cadastroRequest.getTransactionId();

        Map<String, String> headers = new HashMap<>(cadastroRequest.getHeaders());
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);

        cadastro.setEfetivado(false);
        cadastro.setSituacao(Resultado.ERRO);
        cadastro.setProcessado(true);
        var criticas = new HashSet<Critica>();

        criticas.add(critica);

        if(Objects.nonNull(cadastro.getCriticas()) && !cadastro.getCriticas().isEmpty()){
            criticas.addAll(cadastro.getCriticas());
        }

        cadastro.setCriticas(criticas);
        Cadastro cadastroAtualizado = cadastroRepository.save(cadastro);

        return IdempotentResponse
                .<Cadastro>builder()
                .value(cadastroAtualizado)
                .errorResponse(cadastroAtualizado.getSituacao().equals(Resultado.ERRO))
                .headers(headers)
                .build();
    }
}
