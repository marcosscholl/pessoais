package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;

import java.time.LocalDateTime;
import java.util.List;

import static io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils.objectMapper;

public class SolicitacaoResponseDTOFactory {

    public static SolicitacaoResponseDTO solicitacaoResponseDTOPendenteCadastroMinimoSucesso() {

        return io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO.builder()
                .idTransacao("2025022743672130447801610323")
                .canal("FOLHA_IB")
                .numCooperativa("0167")
                .numAgencia("17")
                .codConvenioFontePagadora("3AO")
                .cnpjFontePagadora("18523110000101")
                .status("PENDENTE")
                .resultado("RECEBIDO")
                .critica(false)
                .dataCriacao(LocalDateTime.now())
                .cadastros(List.of(CadastroResponseDTOFactory.cadastroMinimoEmProcessamento()))
                .build();
    }

    public static SolicitacaoResponseDTO solicitacaoResponseDTOPendenteCadastroCompletoSucesso() {

        return io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO.builder()
                .idTransacao("2025022743672130447801610323")
                .canal("FOLHA_IB")
                .numCooperativa("0167")
                .numAgencia("17")
                .codConvenioFontePagadora("3AO")
                //.cnpjFontePagadora("18523110000101")
                .cpfFontePagadora("03104322007")
                .status("PENDENTE")
                .resultado("RECEBIDO")
                .critica(false)
                .dataCriacao(LocalDateTime.now())
                .cadastros(List.of(CadastroResponseDTOFactory.cadastroCompletoEmProcessamento()))
                .build();
    }

    public static SolicitacaoResponseDTO solicitacaoResponseDTO(Solicitacao solicitacao) {
        return objectMapper().convertValue(solicitacao, SolicitacaoResponseDTO.class);
    }

    public static SolicitacaoResponseDTO solicitacaoResponseDTODoisCadastros(){
        return solicitacaoResponseDTO(SolicitacaoFactory.solicitacaoPendenteDoisCadastros());
    }
}