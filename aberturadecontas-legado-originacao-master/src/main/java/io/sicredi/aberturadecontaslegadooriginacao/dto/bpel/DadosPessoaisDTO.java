package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DadosPessoaisDTO(String nome,
                               String nomeSocial,
                               String cpf,
                               String naturalidadeCidade,
                               String naturalidadeEstado,
                               String nacionalidade,
                               LocalDate dataNascimento,
                               String genero,
                               String canalComunicacaoPreferencial,
                               String estadoCivil,
                               Boolean uniaoEstavel,
                               String regimeCasamento,
                               ConjugeDTO conjuge,
                               Boolean residenciaExterior,
                               List<ResidenciaExteriorDTO> residenciasExterior,
                               List<EnderecoDTO> enderecos,
                               List<EmailInfoDTO> emails,
                               IdentificacaoDTO identificacao,
                               List<ParenteDTO> parentes,
                               List<TelefoneDTO> telefones,
                               List<ReferenciaDTO> referencias) {
}