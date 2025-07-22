package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sicredi.aberturadecontaslegadooriginacao.config.CustomLocalDateTimeIsoDeserializer;

import java.time.LocalDateTime;
import java.util.List;

public record AcquisitionOrdersDTO(@JsonProperty("id") String id,
                                   @JsonProperty("offerId") String idOferta,
                                   @JsonProperty("organization") String cooperativa,
                                   @JsonProperty("agency") String agencia,
                                   @JsonProperty("status") StatusPedido status,
                                   @JsonProperty("trackerStatus") String trackerStatus,
                                   @JsonProperty("createdBy") String criadoPor,
                                   @JsonProperty("originSource") String canal,
                                   @JsonProperty("enrollmentType") String tipoInscricao,
                                   @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class)
                                   @JsonProperty("startDate") LocalDateTime dataInicio,
                                   @JsonProperty("items") List<ProdutoDTO> produtos,
                                   @JsonProperty("portfolioManagementId") String codigoCarteira) {
}