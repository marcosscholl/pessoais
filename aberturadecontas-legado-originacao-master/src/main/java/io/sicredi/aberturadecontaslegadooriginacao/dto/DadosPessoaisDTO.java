package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDate;
import java.util.*;

public record DadosPessoaisDTO(@JsonProperty("addresses") List<EnderecoDTO> enderecos,
                               @JsonProperty("birthCity") String naturalidadeCidade,
                               @JsonProperty("birthCountry") String nacionalidade,
                               @JsonProperty("birthDate") LocalDate dataNascimento,
                               @JsonProperty("birthState") String naturalidadeEstado,
                               @JsonProperty("emails") List<EmailDTO> emails,
                               @JsonProperty("gender") Genero genero,
                               @JsonProperty("idCard") IdentificacaoDTO identificacao,
                               @JsonProperty("mainContactChannel") PreferenciaComunicacao canalComunicacaoPreferencial,
                               @JsonProperty("maritalStatus") EstadoCivil estadoCivil,
                               @JsonProperty("commonLawMarriage") Boolean uniaoEstavel,
                               @JsonProperty("propertyRegime") RegimeCasamento regimeCasamento,
                               @JsonProperty("spouse") ConjugeDTO conjuge,
                               @JsonProperty("name") NomeCompletoDTO nomeCompleto,
                               @JsonProperty("parents") List<ParenteDTO> parentes,
                               @JsonProperty("phones") List<TelefoneDTO> telefones,
                               @JsonProperty("references") List<ReferenciaDTO> referencias,
                               @JsonProperty("suid") String cpf,
                               @JsonProperty("taxResidence") Boolean residenciaExterior,
                               @JsonProperty("taxResidenceCountries") List<ResidenciaExteriorDTO> residenciasExterior) {
}