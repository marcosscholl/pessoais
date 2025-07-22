package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils.objectMapper;

public class SolicitacaoFactory {

    public static Solicitacao solicitacaoPendenteDoisCadastros() {
        return solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
    }

    public static Solicitacao solicitacaoPendenteMenorDeIdadeSemRepresentante() {
        var solicitacao = solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        cadastro.setDataNascimento(LocalDate.now().minusYears(10));
        cadastro.setRepresentante(null);
        solicitacao.setCadastros(List.of(cadastro));
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteMenorDeIdadeComRepresentante() {
        var solicitacao = solicitacaoPendenteMenorDeIdadeSemRepresentante();
        solicitacao.getCadastros().getFirst().setRepresentante(Representante.builder()
                .cpf("48606049034")
                .nome("Representante Cadastro Conta Salário")
                .build());
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteMaiorDeIdadeComRepresentante() {
        var solicitacao = solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        solicitacao.getCadastros().getFirst().setRepresentante(Representante.builder()
                .cpf("48606049034")
                .nome("Representante Cadastro Conta Salário")
                .build());
        solicitacao.setCadastros(List.of(cadastro));
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTO solicitacaoRequestDTO) {

        var solicitacao = Solicitacao.builder()
                .id(1L)
                .idTransacao(new BigInteger("2025022743671603934576222386"))
                .canal("FOLHA_IB")
                .numCooperativa(solicitacaoRequestDTO.numCooperativa())
                .numAgencia(solicitacaoRequestDTO.numAgencia())
                .cnpjFontePagadora(solicitacaoRequestDTO.cnpjFontePagadora())
                .branchCode("ACA")
                .codConvenioFontePagadora("3AO")
                .critica(false)
                .status(Status.PENDENTE)
                .resultado(Resultado.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(null)
                .configuracao(ConfiguracaoFactory.configuracaoValida())
                .build();

        var cadastros = map(solicitacaoRequestDTO.cadastros(), solicitacao);
        solicitacao.setCadastros(cadastros);

        return solicitacao;
    }

    private static List<Cadastro> map(List<CadastroRequestDTO> cadastros, Solicitacao solicitacao) {
        var cadastroList = new ArrayList<Cadastro>();

        cadastros.forEach(cadastroRequestDTO -> {
            var cadastro = objectMapper().convertValue(cadastroRequestDTO, Cadastro.class).toBuilder()
                    .id(1L)
                    .processado(false)
                    .efetivado(false)
                    .situacao(Resultado.RECEBIDO)
                    .dadosRF(DadosRFFactory.maiorDeIdadeRegular())
                    .solicitacao(solicitacao)
                    .build();
            cadastroList.add(cadastro);
        });

        return cadastroList;
    }

    public static Solicitacao solicitacaoPendenteDoisCadastrosConcluidos() {
        var solicitacao = solicitacaoPendenteDoisCadastros();

        solicitacao.getCadastros().forEach(cadastro -> {
            cadastro.setProcessado(true);
            cadastro.setEfetivado(true);
            cadastro.setSituacao(Resultado.CONCLUIDO);
            cadastro.setConta("1234567");
        });
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteDoisCadastrosConcluidosParcialmente() {
        var solicitacao = solicitacaoPendenteDoisCadastrosConcluidos();
        solicitacao.getCadastros().forEach(cadastro -> {
            cadastro.setSituacao(Resultado.CONCLUIDO_PARCIALMENTE);
            cadastro.setCriticas(Set.of(Critica.builder()
                    .tipo(TipoCritica.INFORMATIVO)
                    .codigo("COD0001")
                    .descricao("Crítica genérica")
                    .build()));
        });
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteDoisCadastrosComErro() {
        var solicitacao = solicitacaoPendenteDoisCadastrosConcluidos();
        solicitacao.getCadastros().forEach(cadastro -> {
           cadastro.setSituacao(Resultado.ERRO);
            cadastro.setCriticas(Set.of(Critica.builder()
                    .tipo(TipoCritica.BLOQUEANTE)
                    .codigo("COD0001")
                    .descricao("Crítica bloqueante")
                    .build()));
        });
        return solicitacao;
    }

    public static Solicitacao solicitacaoPendenteCadastroErroContaExistenteNoConvenio() {
        Solicitacao solicitacao = solicitacaoPendenteMaiorDeIdadeComRepresentante();
        Cadastro cadastro = solicitacao.getCadastros().getFirst();
        Critica critica = Critica.builder()
                .codigo("0001")
                .descricao("ASSOCIADO JA POSSUI CONTA SALARIO PARA O CONVENIO INFORMADO")
                .tipo(TipoCritica.BLOQUEANTE)
                .build();
        cadastro.setRepresentante(null);
        cadastro.setProcessado(true);
        cadastro.setEfetivado(false);
        cadastro.setSituacao(Resultado.ERRO);
        cadastro.setConta(null);
        cadastro.setCriticas(Set.of(critica));

        return solicitacao;
    }
}
