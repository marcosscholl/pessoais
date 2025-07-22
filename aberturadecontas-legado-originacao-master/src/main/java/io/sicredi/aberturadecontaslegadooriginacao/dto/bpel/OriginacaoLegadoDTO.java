package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OriginacaoLegadoDTO(String idPedido,
                                  String solicitacaoSiebel,
                                  String cooperativa,
                                  String agencia,
                                  String codigoEntidade,
                                  String status,
                                  String canal,
                                  String origem,
                                  String criadoPor,
                                  List<CadastroDTO> cadastros,
                                  List<DocumentoDTO> documentos,
                                  List<String> produtos,
                                  Map<String, DetalheProdutoDTO> detalheProduto,
                                  String codigoCarteira,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime dataCriacao,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime dataAtualizacao) {
}