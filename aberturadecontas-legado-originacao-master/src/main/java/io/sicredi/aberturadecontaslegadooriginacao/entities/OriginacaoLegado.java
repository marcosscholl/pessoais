package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.ACQUISITION_ORDER_CARTEIRA;
import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.ORIGINACAO_LEGADO;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "originacao-legado")
public class OriginacaoLegado {

    @Id
    private String id;
    @Indexed(sparse = true, background = true)
    private String idPedido;
    private String solicitacaoSiebel;
    @Indexed(sparse = true, background = true)
    private String cooperativa;
    @Indexed(sparse = true, background = true)
    private String agencia;
    @Indexed(sparse = true, background = true)
    private String codigoEntidade;
    private String siglaEstado;
    private String nomeCidade;
    @Indexed(sparse = true, background = true)
    private String status;
    private String canal;
    private String origem;
    private String criadoPor;
    private List<Cadastro> cadastros;
    private List<String> produtos;
    private List<Documento> documentos;
    private Map<String, DetalheProduto> detalheProduto;
    private List<Critica> criticas;
    private String codigoCarteira;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Indexed(sparse = true, background = true)
    private LocalDateTime dataCriacao;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Indexed(sparse = true, background = true)
    private LocalDateTime dataAtualizacao;

    public OriginacaoLegado(String idPedido) {
        this.idPedido = idPedido;
        this.dataCriacao = LocalDateTime.now();
    }

    public boolean isCompleto() {
        return StringUtils.isNotBlank(id) &&
               StringUtils.isNotBlank(idPedido) &&
               StringUtils.isNotBlank(cooperativa) &&
               StringUtils.isNotBlank(agencia) &&
               StringUtils.isNotBlank(codigoEntidade) &&
               StringUtils.isNotBlank(codigoCarteira) &&
               StringUtils.isNotBlank(status) &&
               StringUtils.isNotBlank(canal) &&
               StringUtils.isNotBlank(origem) &&
               StringUtils.isNotBlank(criadoPor) &&
               !CollectionUtils.isEmpty(cadastros) &&
               !CollectionUtils.isEmpty(documentos) &&
               !CollectionUtils.isEmpty(produtos) &&
               !CollectionUtils.isEmpty(detalheProduto) &&
               CollectionUtils.isEmpty(criticas) || (criticas.size() == 1 && temCritica(ORIGINACAO_LEGADO)) &&
               dataCriacao != null;
    }

    public void adicionarCritica(final Critica critica) {
        if (this.criticas == null) {
            this.criticas = new ArrayList<>(0);
        }
        var temCritica = this.criticas.stream().anyMatch(crit -> crit.getCodigo().equals(critica.getCodigo()));
        if (!temCritica) {
            this.criticas.add(critica);
        }
    }

    public void removerCritica(final EtapaProcessoOriginacao etapaProcesso) {
        if (this.criticas != null && !this.criticas.isEmpty()) {
            this.criticas.removeIf(critica -> critica.getCodigo().equals(etapaProcesso));
        }
    }

    public void limparCriticas() {
        this.criticas = new ArrayList<>(0);
    }

    public boolean temCritica(EtapaProcessoOriginacao codigo) {
        return this.criticas != null && this.criticas.stream().anyMatch(critica -> codigo.equals(critica.getCodigo()));
    }

    public boolean acquisitionOrderProcessado() {
        return StringUtils.isNotBlank(idPedido) &&
               StringUtils.isNotBlank(cooperativa) &&
               StringUtils.isNotBlank(agencia) &&
               StringUtils.isNotBlank(status) &&
               StringUtils.isNotBlank(canal) &&
               StringUtils.isNotBlank(origem) &&
               StringUtils.isNotBlank(criadoPor) &&
               cadastros != null && !cadastros.isEmpty() &&
               produtos != null && !produtos.isEmpty() &&
               !temCritica(ACQUISITION_ORDER_CARTEIRA) &&
               detalheProduto != null && !detalheProduto.isEmpty();
    }

    public boolean customerDataProcessado() {
        return cadastros != null && !cadastros.isEmpty() && cadastros.stream().allMatch(Cadastro::isValido);
    }

    public boolean registerDataProcessado() {
        return documentos != null && !documentos.isEmpty();
    }

    public boolean originacaoFisitalLegadoProcessado() {
        return "CANCELADO".equalsIgnoreCase(status) || "FINALIZADO".equalsIgnoreCase(status);
    }

    public boolean gestentProcessado() {
        return codigoEntidade != null;
    }

    public Boolean configuracaoCapitalLegacyProcessado() {
        if (detalheProduto == null || detalheProduto.isEmpty()) {
            return Boolean.FALSE;
        }

        return detalheProduto
                .entrySet()
                .stream()
                .filter(item -> "CAPITAL_COMMERCIAL_PLAN_LEGACY".equals(item.getKey()) || "CAPITAL_LEGACY".equals(item.getKey()))
                .allMatch(config -> !Objects.isNull(config.getValue().getConfiguracao()) && !Objects.isNull(config.getValue().getConfiguracao().getCapital()));
    }

    public Boolean configuracaoAccountLegacyProcessado() {
        if (Objects.isNull(detalheProduto)) {
            return Boolean.FALSE;
        }

        var detalheProdutoAccountLegacy = detalheProduto.get("ACCOUNT_LEGACY");
        if (Objects.isNull(detalheProdutoAccountLegacy)) {
            return Boolean.FALSE;
        }

        var numeroConta = detalheProdutoAccountLegacy.getNumeroConta();
        var configuracao = detalheProdutoAccountLegacy.getConfiguracao();
        var cestaRelacionamento = Objects.nonNull(configuracao) ? configuracao.getCestaRelacionamento() : null;

        return !StringUtils.isBlank(numeroConta)
               && Objects.nonNull(cestaRelacionamento)
               && cestaRelacionamento.valido();
    }

    public Boolean temProduto(String nomeProduto) {
        return Objects.nonNull(detalheProduto) && Objects.nonNull(detalheProduto.get(nomeProduto));
    }

    public Boolean temProdutoLegado() {
        return temProduto("ACCOUNT_LEGACY")
               || temProduto("INVESTMENT_LEGACY")
               || temProduto("CAPITAL_LEGACY")
               || temProduto("CAPITAL_COMMERCIAL_PLAN_LEGACY");
    }

    public void atualizarDetalheProduto(DetalheProduto detalheProduto) {
        getDetalheProduto().remove(detalheProduto.getTipoProduto());
        getDetalheProduto().put(detalheProduto.getTipoProduto(), detalheProduto);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}