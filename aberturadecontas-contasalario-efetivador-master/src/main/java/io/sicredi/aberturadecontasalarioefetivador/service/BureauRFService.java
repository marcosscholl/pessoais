package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontasalarioefetivador.client.bureaurf.BureauRFClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoCritica;
import io.sicredi.aberturadecontasalarioefetivador.mapper.DadosRFMapper;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BureauRFService {

    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String TRANSACTION_ID = "transactionId";
    private static final Set<String> INVALID_SITUATIONS =
            Set.of("Cancelada por Encerramento de Espólio",
                    "Cancelada por Óbito sem Espólio",
                    "Cancelada por Multiplicidade",
                    "Cancelada de Ofício");
    private final BureauRFClient bureauRFClient;
    private final CadastroRepository cadastroRepository;
    private final DadosRFMapper dadosRFMapper;
    private final MetricService metricService;

    @IdempotentTransaction
    public IdempotentResponse<Cadastro> processarSolicitacaoBureauRF(IdempotentAsyncRequest<Cadastro> cadastroRequest) {
        var cadastro = cadastroRepository
                .findById(cadastroRequest.getValue().getId()).orElseThrow(NotFoundException::new);

        var transactionIdSolicitacao = cadastroRequest.getHeaders().get(TRANSACTION_ID_SOLICITACAO);
        var transactionId = cadastroRequest.getTransactionId();
        var headers = new HashMap<>(cadastroRequest.getHeaders());
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);

        log.info("[{}] [{}] - Consultando dados do BureauRF para atualizar o cadastro de Conta Salário. ", transactionIdSolicitacao, transactionId);

        var bureauRFDTO = bureauRFClient.consultaCPF(cadastro.getCpf(),
                cadastro.getSolicitacao().getNumCooperativa() + cadastro.getSolicitacao().getNumAgencia(),
                cadastro.getSolicitacao().getNumCooperativa());

        var cadastroAtualizado = atualizaCadastro(cadastro, bureauRFDTO);
        cadastroAtualizado = cadastroRepository.save(cadastroAtualizado);

        log.info("[{}] [{}] - Consulta dos dados do BureauRF e atualização do cadastro de Conta Salário finalizados com sucesso. ", transactionIdSolicitacao, transactionId);

        return IdempotentResponse
                .<Cadastro>builder()
                .value(cadastroAtualizado)
                .errorResponse(cadastroAtualizado.getSituacao().equals(Resultado.ERRO))
                .headers(headers)
                .build();
    }

    private Cadastro atualizaCadastro(Cadastro cadastro, BureauRFDTO bureauRFDTO) {

        var criticasValidacao = new HashSet<Critica>();
        validaSituacaoCadastral(bureauRFDTO, criticasValidacao);
        validaNomeDiferenteDaRF(cadastro, bureauRFDTO, criticasValidacao);
        validaDataNascimentoDiferenteDaRF(cadastro, bureauRFDTO, criticasValidacao);
        validaSexoDiferenteDaRF(cadastro, bureauRFDTO, criticasValidacao);

        Cadastro cadastroAtualizado = atualizaRepresentante(cadastro, bureauRFDTO);

        cadastroAtualizado.getCriticas().addAll(criticasValidacao);
        cadastroAtualizado.setDadosRF(dadosRFMapper.map(bureauRFDTO));
        cadastroAtualizado.getCriticas().stream()
                .filter(critica -> critica.getTipo().equals(TipoCritica.BLOQUEANTE))
                .findFirst()
                .ifPresent(critica -> {
                    cadastroAtualizado.setEfetivado(false);
                    cadastroAtualizado.setSituacao(Resultado.ERRO);
                    cadastroAtualizado.setProcessado(true);
                });
        return cadastroAtualizado;
    }

    private void validaSituacaoCadastral(BureauRFDTO bureauRFDTO, Set<Critica> criticas) {
        if (INVALID_SITUATIONS.stream()
                .anyMatch(situation -> situation.equalsIgnoreCase(bureauRFDTO.situacaoCadastral()))) {
            criticas.add(Critica.builder()
                    .codigo("RFB001")
                    .descricao("CPF em situação irregular na base Receita Federal.")
                    .tipo(TipoCritica.BLOQUEANTE).build());
            metricService.incrementCounter("bureaurf_rfb001_irregular");

        }
    }

    private void validaNomeDiferenteDaRF(Cadastro cadastro, BureauRFDTO bureauRFDTO, Set<Critica> criticas) {
        if (StringUtils.isNotBlank(cadastro.getNome()) && !cadastro.getNome().equalsIgnoreCase(bureauRFDTO.nome())) {
            criticas.add(Critica.builder()
                    .codigo("RFB004")
                    .descricao("Nome informado no cadastro diferente do nome na Receita Federal.")
                    .tipo(TipoCritica.INFORMATIVO).build());
            metricService.incrementCounter("bureaurf_rfb004_nome_divergente");
        }
    }

    private void validaDataNascimentoDiferenteDaRF(Cadastro cadastro, BureauRFDTO bureauRFDTO, Set<Critica> criticas) {
        if (Objects.nonNull(cadastro.getDataNascimento()) &&  !cadastro.getDataNascimento().isEqual(bureauRFDTO.dataNascimento())){
            criticas.add(Critica.builder()
                    .codigo("RFB005")
                    .descricao("Data de nascimento informada no cadastro diferente da data de nascimento na Receita Federal.")
                    .tipo(TipoCritica.INFORMATIVO).build());
            metricService.incrementCounter("bureaurf_rfb005_data_nascimento_divergente");
        }
    }

    private void validaSexoDiferenteDaRF(Cadastro cadastro, BureauRFDTO bureauRFDTO, Set<Critica> criticas) {
        if (StringUtils.isNotBlank(cadastro.getFlgSexo()) &&
                StringUtils.isNotBlank(bureauRFDTO.sexo()) &&
                !cadastro.getFlgSexo().equalsIgnoreCase(bureauRFDTO.sexo().substring(0,1))){
            criticas.add(Critica.builder()
                    .codigo("RFB006")
                    .descricao("Sexo do cliente informado no cadastro diferente do sexo na Receita Federal.")
                    .tipo(TipoCritica.INFORMATIVO).build());
            metricService.incrementCounter("bureaurf_rfb006_sexo_divergente");
        }
    }

    private Cadastro atualizaRepresentante(Cadastro cadastro, BureauRFDTO bureauRFDTO) {
        Cadastro cadastroAtualizado = cadastro.toBuilder().build();

        if (LocalDate.now().minusYears(18).isBefore(bureauRFDTO.dataNascimento())) {
            if (Objects.isNull(cadastroAtualizado.getRepresentante()) ||
                    StringUtils.isBlank(cadastroAtualizado.getRepresentante().getCpf())) {

                cadastroAtualizado.getCriticas().add(Critica.builder()
                        .codigo("RFB002")
                        .descricao("Cadastro de menor de idade informado sem representante legal.")
                        .tipo(TipoCritica.BLOQUEANTE).build());
                metricService.incrementCounter("bureaurf_rfb002_menor_sem_representante");
            } else {
                var representanteBureauRFDTO = bureauRFClient.consultaCPF(cadastroAtualizado.getRepresentante().getCpf(),
                        cadastroAtualizado.getSolicitacao().getNumCooperativa() + cadastroAtualizado.getSolicitacao().getNumAgencia(),
                        cadastroAtualizado.getSolicitacao().getNumCooperativa());

                var criticasValidacaoRepresentante = validaDadosRepresentanteNoBureauRF(representanteBureauRFDTO);
                cadastroAtualizado.getCriticas().addAll(criticasValidacaoRepresentante);

                if (Strings.isBlank(cadastro.getRepresentante().getNome())) {
                    cadastroAtualizado.getRepresentante().setNome(representanteBureauRFDTO.nome());
                }
            }
        }
        else if(Objects.nonNull(cadastro.getRepresentante()) &&
                StringUtils.isNotBlank(cadastro.getRepresentante().getCpf())){
            cadastroAtualizado.getCriticas().add(Critica.builder()
                    .codigo("RFB008")
                    .descricao("Titular em maioridade, representante não cadastrado.")
                    .tipo(TipoCritica.INFORMATIVO).build());
            metricService.incrementCounter("bureaurf_rfb008_maior_com_representante");
        }
        return cadastroAtualizado;
    }

    private Set<Critica> validaDadosRepresentanteNoBureauRF(BureauRFDTO bureauRFDTO) {
        var criticas = new HashSet<Critica>();
        validaSituacaoCadastral(bureauRFDTO, criticas);
        validaRepresentanteMaiorDeIdade(bureauRFDTO, criticas);
        return criticas;
    }

    private void validaRepresentanteMaiorDeIdade(BureauRFDTO bureauRFDTO, Set<Critica> criticas) {
        if (LocalDate.now().minusYears(18).isBefore(bureauRFDTO.dataNascimento())){
            criticas.add(Critica.builder()
                    .codigo("RFB003")
                    .descricao("Representante legal informado é menor de idade.")
                    .tipo(TipoCritica.BLOQUEANTE).build());
            metricService.incrementCounter("bureaurf_rfb003_representante_menor");
        }
    }
}
