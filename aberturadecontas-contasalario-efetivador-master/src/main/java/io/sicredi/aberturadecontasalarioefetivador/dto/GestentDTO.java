package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public record GestentDTO(
        List<ContentRecord> content,
        PageableRecord pageable,
        boolean last,
        int totalPages,
        int totalElements,
        SortRecord sort,
        int size,
        int number,
        int numberOfElements,
        boolean first,
        boolean empty
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentRecord(
            int idEntidadeSicredi,
            String codigoTipoEntidade,
            String codigoCooperativa,
            String codigoAgencia,
            String nomeFantasia,
            String numeroCnpj,
            String codigoSituacao,
            String codigoEntidade,
            String codigoEntidadePai,
            String codigoCredis,
            String codigoCaf,
            String dataAbertura,
            String dataCentroCusto,
            boolean flagSejaAssociado,
            boolean flagDigital
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageableRecord(
            int pageNumber,
            int pageSize,
            SortRecord sort,
            int offset,
            boolean paged,
            boolean unpaged
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SortRecord(
            boolean empty,
            boolean sorted,
            boolean unsorted
    ) {}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}