package io.sicredi.aberturadecontaslegadooriginacao.testdata;

import io.sicredi.aberturadecontaslegadooriginacao.dto.*;
import io.sicredi.aberturadecontaslegadooriginacao.json.*;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.settings.*;
import org.springframework.integration.support.*;
import org.springframework.kafka.support.*;
import org.springframework.messaging.*;

public class TestDataFactory {

    private static final String COD_CONTA_CAPITAL_LEGADO = "CSOC";
    private static final String CONTA_CAPITAL_LEGADO = "CAPITAL_LEGACY";
    private static final String COD_CONTA_CORRENTE_INDIVIDUAL = "53";
    private static final String CONTA_CORRENTE_INDIVIDUAL = "INDIVIDUAL_CHECKING_ACCOUNT";
    private static final String COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO = "INVST";
    private static final String CONTA_POUPANCA_INDIVIDUAL_LEGADO = "INVESTMENT_LEGACY";
    private static final StatusProduto STATUS_PRODUTO_INICIADO = StatusProduto.STARTED;
    private static final StatusProduto STATUS_PRODUTO_PENDENTE = StatusProduto.PENDING;

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOSomenteCapitalLegacyStarted() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_CAPITAL_LEGADO, CONTA_CAPITAL_LEGADO, 1,
                                                   STATUS_PRODUTO_INICIADO);
    }

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOSomenteCapitalLegacyPending() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_CAPITAL_LEGADO, CONTA_CAPITAL_LEGADO, 1,
                                                   STATUS_PRODUTO_PENDENTE);
    }

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOSomenteIndividualCheckingAccountStarted() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_CORRENTE_INDIVIDUAL, CONTA_CORRENTE_INDIVIDUAL, 1,
                                                   STATUS_PRODUTO_INICIADO);
    }

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOSomenteInvestmentLegacyStarted() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO,
                                                   CONTA_POUPANCA_INDIVIDUAL_LEGADO, 0, STATUS_PRODUTO_INICIADO);
    }

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOSomenteInvestmentLegacyPending() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO,
                                                   CONTA_POUPANCA_INDIVIDUAL_LEGADO, 0,
                                                   STATUS_PRODUTO_PENDENTE);
    }

    public static AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemDTOInvestmentLegacyStartedComProdutosRelacionados() {
        return getAcquisitionEngineManagerItemsDTO(COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO,
                                                   CONTA_POUPANCA_INDIVIDUAL_LEGADO, 1,
                                                   STATUS_PRODUTO_INICIADO);
    }

    private static AcquisitionEngineManagerItemsEventDTO getAcquisitionEngineManagerItemsDTO(String codigoProduto,
            String tipoProduto, int numeroDeProdutosRelacionados, StatusProduto statusProduto) {
        return Instancio.of(AcquisitionEngineManagerItemsEventDTO.class)
                        .withSettings(settings())
                        .generate(Select.field(PedidoDTO::produtosRelacionados),
                                  gen -> gen.collection().minSize(numeroDeProdutosRelacionados)
                                            .maxSize(numeroDeProdutosRelacionados))
                        .generate(Select.field(ProdutoDTO::cadastros),
                                  gen -> gen.collection().minSize(1).maxSize(1))
                        .set(Select.field(ProdutoDTO::codigoProduto), codigoProduto)
                        .set(Select.field(ProdutoDTO::tipoProduto), tipoProduto)
                        .set(Select.field(ProdutoDTO::status), statusProduto)
                        .set(Select.field(CadastroDTO::titularPrincipal), Boolean.TRUE)
                        .create();
    }

    public static Message<String> eventoSomenteCapitalLegacyPending(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_CAPITAL_LEGADO, STATUS_PRODUTO_PENDENTE,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOSomenteCapitalLegacyPending());
    }

    public static Message<String> eventoSomenteCapitalLegacyStarted(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_CAPITAL_LEGADO, STATUS_PRODUTO_INICIADO,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOSomenteCapitalLegacyStarted());
    }

    public static Message<String> eventoSomenteIndividualCheckingAccountStarted(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_CORRENTE_INDIVIDUAL, STATUS_PRODUTO_INICIADO,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOSomenteIndividualCheckingAccountStarted());
    }

    public static Message<String> eventoSomenteInvestmentLegacyStarted(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_POUPANCA_INDIVIDUAL_LEGADO, STATUS_PRODUTO_INICIADO,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOSomenteInvestmentLegacyStarted());
    }

    public static Message<String> eventoSomenteInvestmentLegacyPending(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_POUPANCA_INDIVIDUAL_LEGADO, STATUS_PRODUTO_PENDENTE,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOSomenteInvestmentLegacyPending());
    }

    public static Message<String> eventoInvestmentLegacyStartedComProdutosRelacionados(Acknowledgment acknowledgment) {
        return acquisitionEngineManagerItemsEvent(CONTA_POUPANCA_INDIVIDUAL_LEGADO, STATUS_PRODUTO_INICIADO,
                                                  acknowledgment,
                                                  acquisitionEngineManagerItemDTOInvestmentLegacyStartedComProdutosRelacionados());
    }

    private static Message<String> acquisitionEngineManagerItemsEvent(String tipoProduto, StatusProduto status,
            Acknowledgment acknowledgment,
            AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemsEventDTO) {
        return MessageBuilder
                .withPayload(JsonUtils.objetoParaJson(acquisitionEngineManagerItemsEventDTO))
                .setHeader("status", status)
                .setHeader(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment)
                .setHeader("tipoProduto", tipoProduto)
                .setHeader("contentType", "application/json")
                .build();
    }

    private static Settings settings() {
        return Settings.create()
                       .set(Keys.MAX_DEPTH, 10)
                       .set(Keys.COLLECTION_MIN_SIZE, 1)
                       .set(Keys.COLLECTION_MAX_SIZE, 1);
    }

    public static AcquisitionOrdersDTO acquisitionOrdersDTO() {
        return Instancio.create(AcquisitionOrdersDTO.class);
    }

    public static CustomerDataDTO customerDataDTO() {
        return Instancio.create(CustomerDataDTO.class);
    }

    public static RegisterDataDTO registerDataDTO() {
        return Instancio.create(RegisterDataDTO.class);
    }

    public static ConfiguracaoDTO configuracaoDTO() {
        return Instancio.create(ConfiguracaoDTO.class);
    }
}