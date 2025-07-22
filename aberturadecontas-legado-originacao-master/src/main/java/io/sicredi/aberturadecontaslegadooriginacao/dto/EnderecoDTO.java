package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sicredi.aberturadecontaslegadooriginacao.config.CustomLocalDateTimeIsoDeserializer;

import java.time.LocalDateTime;

public record EnderecoDTO(@JsonProperty("id") String id,
                          @JsonProperty("addressType") TipoEndereco tipo,
                          @JsonProperty("allowDelivery") Boolean permiteCorrespondencia,
                          @JsonProperty("city") String cidade,
                          @JsonProperty("countryCode") Integer codigoPais,
                          @JsonProperty("countryDescription") String descricaoPais,
                          @JsonProperty("mainAddress") Boolean enderecoPrincipal,
                          @JsonProperty("neighborhood") String bairro,
                          @JsonProperty("noNumber") Boolean semNumero,
                          @JsonProperty("number") String numero,
                          @JsonProperty("postalCode") String cep,
                          @JsonProperty("sbn") String complemento,
                          @JsonProperty("source") String origem,
                          @JsonProperty("state") String estado,
                          @JsonProperty("street") String logradouro,
                          @JsonProperty("streetType") String tipoLogradouro,
                          @JsonProperty("registerDate")
                          @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataCriacao,
                          @JsonProperty("changeDate")
                          @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataAtualizacao) {
}