package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.EnriquecimentoCadastroResultado;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EnriquecimentoCadastroService {

    private final CadastroAssociadoService cadastroAssociadoService;
    private final EmailService emailService;
    private final TelefoneService telefoneService;
    private final CadastroRepository cadastroRepository;

    public EnriquecimentoCadastroResultado processarCadastro(Cadastro cadastroEvent, String transactionIdSolicitacao, String transactionId) {

        var processamento = new EnriquecimentoCadastroResultado();
        processamento.setEmail(StringUtils.isNotBlank(cadastroEvent.getEmail()));
        processamento.setTelefone(StringUtils.isNotBlank(cadastroEvent.getTelefone()));
        processamento.setCriticas(new ArrayList<>());

        if (!processamento.isEmail() && !processamento.isTelefone()) {
            return processamento;
        }

        log.info("[{}][{}] - Iniciando enriquecimento de cadastro com telefone e email. cpf: {}",
                transactionIdSolicitacao, transactionId, cadastroEvent.getCpf());

        Cadastro cadastro = cadastroRepository.findById(cadastroEvent.getId()).orElseThrow(NotFoundException::new);

        Optional<DadosAssociado> dadosAssociadoOpt = consultarDadosAssociado(cadastro, processamento);
        if (dadosAssociadoOpt.isEmpty()) {
            return processamento;
        }
        cadastro.setOidPessoa(dadosAssociadoOpt.get().getOidPessoa());
        cadastroRepository.save(cadastro);

        DadosAssociado dadosAssociado = dadosAssociadoOpt.get();

        if (processamento.isEmail()){
            log.info("[{}][{}] - Iniciando processamento de email do cadastro.", transactionIdSolicitacao, transactionId);
            processamento.setEmailCriado(processarEmail(cadastro, dadosAssociado, processamento));
        }
        else {
            processamento.setEmailCriado(false);
        }

        if(processamento.isTelefone()){
            log.info("[{}][{}] - Iniciando processamento de telefone do cadastro.", transactionIdSolicitacao, transactionId);
            processamento.setTelefoneCriado(processarTelefone(cadastro, dadosAssociado, processamento));
        }
        else {
            processamento.setTelefoneCriado(false);
        }
        return processamento;
    }

    private Optional<DadosAssociado> consultarDadosAssociado(Cadastro cadastro, EnriquecimentoCadastroResultado result) {
        try {
            var response = cadastroAssociadoService.consultarDadosAssociado(cadastro.getCpf());
            var elementos = response.getOutConsultarDadosAssociado().getElementos();
            return (elementos != null && !elementos.isEmpty())
                    ? Optional.of(elementos.get(0))
                    : Optional.empty();
        } catch (Exception e) {
            criaMensagemErroEGeraLog(String.format("Erro ao consultar os dados do CPF %s : %s", cadastro.getCpf(), e.getMessage()), e, result);
            return Optional.empty();
        }
    }

    private boolean processarEmail(Cadastro cadastro, DadosAssociado associado, EnriquecimentoCadastroResultado result) {
        try {
            var emailsResponse = emailService.consultarEmail(associado);
            boolean emailExists = emailsResponse.getListaEmail() != null &&
                    emailsResponse.getListaEmail().getEmail() != null &&
                    !emailsResponse.getListaEmail().getEmail().isEmpty();
            return !emailExists && salvarEmail(associado, cadastro.getEmail(), cadastro.getSolicitacao().getBranchCode(), result);
        } catch (Exception e) {
            return criaMensagemELogERetornaFalse("Erro ao consultar email para o CPF %s : %s", cadastro.getCpf(), e, result);
        }
    }

    private boolean salvarEmail(DadosAssociado associado, String email, String codigoEntidade, EnriquecimentoCadastroResultado result) {
        try {
            emailService.salvarEmailNovo(associado, email, codigoEntidade);
            return true;
        } catch (Exception e) {
            return criaMensagemELogERetornaFalse("Erro ao criar o email para o CPF %s : %s", associado.getNroDocumento(), e, result);
        }
    }

    private boolean processarTelefone(Cadastro cadastro, DadosAssociado associado, EnriquecimentoCadastroResultado result) {
        try {
            var telefonesResponse = telefoneService.consultarTelefones(associado);
            boolean telefoneExists = telefonesResponse.getListaTelefone() != null &&
                    telefonesResponse.getListaTelefone().getTelefone() != null &&
                    !telefonesResponse.getListaTelefone().getTelefone().isEmpty();
            return !telefoneExists && salvarTelefone(associado, cadastro.getTelefone(), cadastro.getSolicitacao().getBranchCode(), result);
        } catch (Exception e) {
            return criaMensagemELogERetornaFalse("Erro ao consultar o telefone para o CPF %s : %s", cadastro.getCpf(), e, result);
        }
    }

    private boolean salvarTelefone(DadosAssociado associado, String telefone, String codigoEntidade, EnriquecimentoCadastroResultado result) {
        try {
            telefoneService.salvarNovoTelefone(associado, codigoEntidade, telefone);
            return true;
        } catch (Exception e) {
            return criaMensagemELogERetornaFalse("Erro ao criar o telefone para o CPF %s : %s", associado.getNroDocumento(), e, result);
        }
    }

    private static boolean criaMensagemELogERetornaFalse(String format, String associado, Exception e, EnriquecimentoCadastroResultado result) {
        criaMensagemErroEGeraLog(String.format(format, associado, e.getMessage()), e, result);
        return false;
    }

    private static void criaMensagemErroEGeraLog(String msg, Exception e, EnriquecimentoCadastroResultado result) {
        log.error(msg, e);
        result.getCriticas().add(msg);
    }
}
