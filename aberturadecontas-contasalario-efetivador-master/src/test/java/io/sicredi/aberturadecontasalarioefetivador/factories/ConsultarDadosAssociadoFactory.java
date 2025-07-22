package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociado;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociadoResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto.ConsultarDadosAssociadoBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto.DadosAssociadoBuilder;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto.OutConsultarDadosAssociadoBuilder;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.utils.DateUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConsultarDadosAssociadoFactory {

    public static ConsultarDadosAssociado consultarDadosAssociado(Cadastro cadastro){
        return ConsultarDadosAssociadoBuilder.builder()
                .cpf(cadastro.getCpf())
                .build();
    }

    public static ConsultarDadosAssociadoResponse consultarDadosAssociadoResponse(Cadastro cadastro){
        DadosAssociadoBuilder dadosAssociado = DadosAssociadoBuilder.builder()
                .nomAssociado(cadastro.getNome())
                .nroDocumento(cadastro.getCpf())
                .datNascimento(DateUtils.converterLocalDateParaXMLGregorian(LocalDate.of(cadastro.getDataNascimento().getYear(), cadastro.getDataNascimento().getMonth(), cadastro.getDataNascimento().getDayOfMonth())))
                .oidPessoa(Long.parseLong(cadastro.getCpf()))
                .build();
        return build(dadosAssociado);
    }

    public static ConsultarDadosAssociadoResponse consultarDadosAssociadoResponseSemElementos(){
        return build(null);
    }

    public static DadosAssociado dadosAssociadoPrimeiro() {
        Solicitacao solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        return consultarDadosAssociadoResponse(solicitacao.getCadastros()
                .getFirst())
                .getOutConsultarDadosAssociado()
                .getElementos()
                .getFirst();
    }

    private static ConsultarDadosAssociadoResponse build(DadosAssociadoBuilder dadosAssociado) {
        ConsultarDadosAssociadoResponse consultarDadosAssociadoResponse = new ConsultarDadosAssociadoResponse();
        consultarDadosAssociadoResponse.setOutConsultarDadosAssociado(OutConsultarDadosAssociadoBuilder.builder()
                .elementos(Objects.nonNull(dadosAssociado) ? List.of(dadosAssociado) : Collections.emptyList())
                .build());
        return consultarDadosAssociadoResponse;
    }

    public static ConsultarDadosAssociadoResponse consultarDadosAssociadoResponseValido() {
        Solicitacao solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        return ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro);
    }
}
