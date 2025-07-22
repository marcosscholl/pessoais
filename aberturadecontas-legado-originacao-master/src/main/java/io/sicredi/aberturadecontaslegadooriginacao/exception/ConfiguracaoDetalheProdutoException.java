package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class ConfiguracaoDetalheProdutoException extends RuntimeException {

    public ConfiguracaoDetalheProdutoException(Exception e) {
        super("Erro ao processar configuração de detalhes de produto.", e);
    }
}