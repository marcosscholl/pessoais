package io.sicredi.aberturadecontasalarioefetivador.config;

import io.sicredi.aberturadecontasalarioefetivador.utils.EscapeSqlInjectionUtils;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
@AllArgsConstructor
public class DatabaseProcessor {

    private DataSource dataSource;

    private final DatabaseProcessorSQLPropertiesConfig oracleSqlProperties;

    @Order(1)
    @PostConstruct
    public void init() {
        if (oracleSqlProperties.isCriartabelasEnabled()) {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

                log.info("DatabaseInitializer - Iniciando a criação de tabelas e índices.");

                // Tabela IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR
                log.info("Criando tabela: IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR");
                stmt.executeUpdate("""
                CREATE TABLE IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR (
                    ID VARCHAR(50) NOT NULL,
                    CHECKSUM NUMBER NOT NULL,
                    CREATED_AT TIMESTAMP NOT NULL,
                    CONSTRAINT IDEMPOTENT_TRANSACTION_PKEY PRIMARY KEY (ID)
                )
            """);

                log.info("Criando índice: IDX_IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR_CREATED_AT");
                stmt.executeUpdate("""
                CREATE INDEX IDX_IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR_CREATED_AT
                ON IDEMPOTENT_TRANSACTION_ABERTURA_CONTA_SALARIO_EFETIVADOR (CREATED_AT)
            """);

                // Tabela IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR
                log.info("Criando tabela: IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR");
                stmt.executeUpdate("""
                CREATE TABLE IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR (
                    TRANSACTION_ID VARCHAR(50) NOT NULL,
                    ID VARCHAR(255) NOT NULL,
                    BODY CLOB,
                    CLASS_NAME VARCHAR2(255),
                    IS_ERROR SMALLINT,
                    CREATED_AT TIMESTAMP NOT NULL,
                    TYPE VARCHAR2(255),
                    HEADERS CLOB,
                    TOPIC_NAME VARCHAR2(255) NOT NULL,
                    SORT_KEY VARCHAR2(255),
                    IS_CDC SMALLINT,
                    CONSTRAINT IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR_PKEY PRIMARY KEY (TRANSACTION_ID, ID)
                )
            """);

                log.info("Criando índices para: IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR");
                stmt.executeUpdate("""
                CREATE INDEX IDX_IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR_CREATED_AT
                ON IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR (CREATED_AT)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR_TRANSACTION_ID
                ON IDEMPOTENT_TRANSACTION_OUTBOX_ABERTURA_CONTA_SALARIO_EFETIVADOR (TRANSACTION_ID)
            """);

                // Tabela CONFIGURACAO
                log.info("Criando tabela: CONFIGURACAO");
                stmt.executeUpdate("""
                CREATE TABLE CONFIGURACAO (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    URL_WEBHOOK VARCHAR2(255),
                    PORTA_HTTP VARCHAR2(10),
                    AUTORIZACAO_RETORNO VARCHAR2(255)
                )
            """);

                // Tabela DOCUMENTO
                log.info("Criando tabela: DOCUMENTO");
                stmt.executeUpdate("""
                CREATE TABLE DOCUMENTO (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    NUM_DOCUMENTO VARCHAR2(50),
                    DATA_EMISSAO_DOC DATE,
                    NOME_ORGAO_EMISSOR_DOC VARCHAR2(50),
                    SGL_UF_EMISSOR_DOC VARCHAR2(2)
                )
            """);

                log.info("Criando índice: IDX_DOCUMENTO_NUM_DOCUMENTO");
                stmt.executeUpdate("""
                CREATE INDEX IDX_DOCUMENTO_NUM_DOCUMENTO ON DOCUMENTO (NUM_DOCUMENTO)
            """);

                // Tabela ENDERECO
                log.info("Criando tabela: ENDERECO");
                stmt.executeUpdate("""
                CREATE TABLE ENDERECO (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    TIPO_LOGRADOURO VARCHAR2(50),
                    NOME_LOGRADOURO VARCHAR2(255),
                    NUM_ENDERECO VARCHAR2(50),
                    TXT_COMPLEMENTO VARCHAR2(255),
                    NOME_BAIRRO VARCHAR2(255),
                    NUM_CEP VARCHAR2(8),
                    NOME_CIDADE VARCHAR2(255),
                    SGL_UF VARCHAR2(2)
                )
            """);

                log.info("Criando índice: IDX_ENDERECO_NUM_CEP");
                stmt.executeUpdate("""
                CREATE INDEX IDX_ENDERECO_NUM_CEP ON ENDERECO (NUM_CEP)
            """);

                // Tabela DADOS_RF
                log.info("Criando tabela: DADOS_RF");
                stmt.executeUpdate("""
                CREATE TABLE DADOS_RF (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    ANO_OBITO VARCHAR2(10 BYTE),
                    CODIGO_SITUACAO_CADASTRAL VARCHAR2(30 BYTE) NOT NULL,
                    DATA_NASCIMENTO DATE NOT NULL,
                    DESC_SITUACAO_CADASTRAL VARCHAR2(255 BYTE),
                    NOME VARCHAR2(255 BYTE) NOT NULL,
                    SEXO VARCHAR2(20 BYTE) NOT NULL,
                    SITUACAO_CADASTRAL VARCHAR2(255 BYTE) NOT NULL
                )
            """);

                // Tabela PORTABILIDADE
                log.info("Criando tabela: PORTABILIDADE");
                stmt.executeUpdate("""
                CREATE TABLE PORTABILIDADE (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    COD_BANCO_DESTINO VARCHAR2(3 BYTE) NOT NULL,
                    NUM_AG_DESTINO VARCHAR2(10 BYTE) NOT NULL,
                    NUM_CONTA_DESTINO VARCHAR2(20) NOT NULL,
                    TIPO_CONTA VARCHAR2(50 BYTE) NOT NULL
                )
            """);

                log.info("Criando índices para: PORTABILIDADE");
                stmt.executeUpdate("""
                CREATE INDEX IDX_PORTABILIDADE_COD_BANCO_DESTINO ON PORTABILIDADE (COD_BANCO_DESTINO)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_PORTABILIDADE_NUM_AG_DESTINO ON PORTABILIDADE (NUM_AG_DESTINO)
            """);

                // Tabela CANAL
                log.info("Criando tabela: CANAL");
                stmt.executeUpdate("""
                CREATE TABLE CANAL (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    CODIGO NUMBER(4) NOT NULL,
                    NOME VARCHAR2(255) NOT NULL,
                    DOCUMENTO VARCHAR2(20),
                    ATIVO NUMBER(1) DEFAULT 1 NOT NULL,
                    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    DATA_ATUALIZACAO TIMESTAMP,
                    CONSTRAINT UK_CANAL_CODIGO UNIQUE (CODIGO),
                    CONSTRAINT UK_CANAL_NOME UNIQUE (NOME)
                )
            """);

                // Tabela SOLICITACAO_CADASTRO_CONTA_SALARIO
                log.info("Criando tabela: SOLICITACAO_CADASTRO_CONTA_SALARIO");
                stmt.executeUpdate("""
                CREATE TABLE SOLICITACAO_CADASTRO_CONTA_SALARIO (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    ID_TRANSACAO VARCHAR2(50) NOT NULL UNIQUE,
                    CANAL VARCHAR2(50),
                    NUM_COOPERATIVA VARCHAR2(5) NOT NULL,
                    NUM_AGENCIA VARCHAR2(5) NOT NULL,
                    BRANCH_CODE VARCHAR2(50),
                    COD_CONVENIO_FONTE_PAGADORA VARCHAR2(50) NOT NULL,
                    CNPJ_FONTE_PAGADORA VARCHAR2(14),
                    CONFIGURACAO_ID NUMBER,
                    STATUS VARCHAR2(50),
                    RESULTADO VARCHAR2(50),
                    CRITICA NUMBER(1) CHECK (CRITICA IN (0, 1)),
                    DATA_CRIACAO TIMESTAMP NOT NULL,
                    DATA_ATUALIZACAO TIMESTAMP,
                    WEBHOOK_HTTP_STATUS_CODIGO VARCHAR2(4),
                    CPF_FONTE_PAGADORA VARCHAR2(11),
                    CONSTRAINT FK_SOLICITACAO_CONFIGURACAO FOREIGN KEY (CONFIGURACAO_ID) REFERENCES CONFIGURACAO (ID)
                )
            """);

                log.info("Criando índices para: SOLICITACAO_CADASTRO_CONTA_SALARIO");
                stmt.executeUpdate("""
                CREATE INDEX IDX_SOLICITACAO_CANAL ON SOLICITACAO_CADASTRO_CONTA_SALARIO (CANAL)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_SOLICITACAO_NUM_COOPERATIVA ON SOLICITACAO_CADASTRO_CONTA_SALARIO (NUM_COOPERATIVA)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_SOLICITACAO_NUM_AGENCIA ON SOLICITACAO_CADASTRO_CONTA_SALARIO (NUM_AGENCIA)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_SOLICITACAO_COD_CONVENIO ON SOLICITACAO_CADASTRO_CONTA_SALARIO (COD_CONVENIO_FONTE_PAGADORA)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_SOLICITACAO_CONFIGURACAO_ID ON SOLICITACAO_CADASTRO_CONTA_SALARIO (CONFIGURACAO_ID)
            """);

                // Tabela CADASTRO
                log.info("Criando tabela: CADASTRO");
                stmt.executeUpdate("""
                CREATE TABLE CADASTRO (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    CPF VARCHAR2(11) NOT NULL,
                    OID_PESSOA NUMBER,
                    NOME VARCHAR2(255),
                    DATA_NASCIMENTO DATE,
                    FLG_SEXO VARCHAR2(1),
                    EMAIL VARCHAR2(70),
                    TELEFONE VARCHAR2(20),
                    SOLICITACAO_ID NUMBER NOT NULL,
                    DOCUMENTO_ID NUMBER,
                    ENDERECO_ID NUMBER,
                    PROCESSADO NUMBER(1) CHECK (PROCESSADO IN (0, 1)),
                    EFETIVADO NUMBER(1) CHECK (EFETIVADO IN (0, 1)),
                    SITUACAO VARCHAR2(50),
                    CONTA VARCHAR2(50),
                    CRITICA VARCHAR2(255),
                    CPF_REPRESENTANTE VARCHAR2(11 BYTE),
                    NOME_REPRESENTANTE VARCHAR2(255 BYTE),
                    DADOSRF_ID NUMBER,
                    PORTABILIDADE_ID NUMBER,
                    CONSTRAINT FK_CADASTRO_SOLICITACAO FOREIGN KEY (SOLICITACAO_ID) REFERENCES SOLICITACAO_CADASTRO_CONTA_SALARIO (ID),
                    CONSTRAINT FK_CADASTRO_DOCUMENTO FOREIGN KEY (DOCUMENTO_ID) REFERENCES DOCUMENTO (ID),
                    CONSTRAINT FK_CADASTRO_ENDERECO FOREIGN KEY (ENDERECO_ID) REFERENCES ENDERECO (ID)
                )
            """);

                log.info("Criando índices para: CADASTRO");
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_CPF ON CADASTRO (CPF)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_SOLICITACAO_ID ON CADASTRO (SOLICITACAO_ID)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_DOCUMENTO_ID ON CADASTRO (DOCUMENTO_ID)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_ENDERECO_ID ON CADASTRO (ENDERECO_ID)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_DADOSRF_ID ON CADASTRO (DADOSRF_ID)
            """);
                stmt.executeUpdate("""
                CREATE INDEX IDX_CADASTRO_PORTABILIDADE_ID ON CADASTRO (PORTABILIDADE_ID)
            """);

                // Tabela CADASTRO_CRITICAS
                log.info("Criando tabela: CADASTRO_CRITICAS");
                stmt.executeUpdate("""
                CREATE TABLE CADASTRO_CRITICAS (
                    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    CADASTRO_ID NUMBER,
                    CODIGO VARCHAR2(30 BYTE),
                    DESCRICAO VARCHAR2(255 BYTE),
                    TIPO VARCHAR2(30 BYTE),
                    CONSTRAINT FK_CRITICAS_CADASTRO_ID FOREIGN KEY (CADASTRO_ID) REFERENCES CADASTRO (ID)
                )
            """);
                log.info("Criando índices para: CADASTRO_CRITICAS");
                stmt.executeUpdate("""
                CREATE INDEX IDX_CRITICAS_CADASTRO_ID ON CADASTRO_CRITICAS (CADASTRO_ID)
            """);

                log.info("DatabaseInitializer - Finalizando com COMMIT.");
                conn.commit();

                log.info("DatabaseInitializer - Tabelas criadas com sucesso.");
            } catch (SQLException e) {
                log.error("DatabaseInitializer - Erro ao criar as tabelas: {}", e.getMessage(), e);
            }
        } else {
            log.info("DatabaseInitializer - Desativado");
        }
    }

    @Order(2)
    @PostConstruct
    public void update() {
        if (oracleSqlProperties.isAtualizarporscriptEnabled()) {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

                log.info("DatabaseUpdater - Iniciando a atualização por script.");

                int quantidade = oracleSqlProperties.getComandos().getQuantidade();
                var querys = oracleSqlProperties.getComandos().getQuerys();

                querys.stream()
                        .limit(quantidade)
                        .forEach(query -> {
                            try {
                                String sanitizedQuery = EscapeSqlInjectionUtils.escapeString(query);
                                log.info("DatabaseUpdater - Executando query: {}", sanitizedQuery);
                                stmt.executeUpdate(sanitizedQuery);
                            } catch (SQLException e) {
                                log.error("DatabaseUpdater - Erro ao executar query: {}", query, e);
                            }
                        });

                log.info("DatabaseUpdater - Finalizando com COMMIT.");
                conn.commit();

                log.info("DatabaseUpdater - Atualização realizada com sucesso.");
            } catch (SQLException e) {
                log.error("DatabaseUpdater - Erro ao atualizar por script: {}", e.getMessage(), e);
            }
        } else {
            log.info("DatabaseUpdater - Desativado");
        }
    }
}