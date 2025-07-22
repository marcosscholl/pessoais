package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SolicitacaoResponseDTO(String idTransacao,
                                     String canal,
                                     String numCooperativa,
                                     String numAgencia,
                                     String codConvenioFontePagadora,
                                     String cnpjFontePagadora,
                                     String cpfFontePagadora,
                                     String status,
                                     String resultado,
                                     Boolean critica,
                                     String webhookHttpStatusCodigo,
                                     @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                     @JsonSerialize(using = LocalDateTimeSerializer.class)
                                     LocalDateTime dataCriacao,
                                     @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                     @JsonSerialize(using = LocalDateTimeSerializer.class)
                                     LocalDateTime dataAtualizacao,
                                     List<CadastroResponseDTO> cadastros) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
