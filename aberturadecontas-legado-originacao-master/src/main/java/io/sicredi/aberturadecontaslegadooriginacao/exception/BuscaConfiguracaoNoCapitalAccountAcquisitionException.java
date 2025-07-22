package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class BuscaConfiguracaoNoCapitalAccountAcquisitionException extends RuntimeException {
    public BuscaConfiguracaoNoCapitalAccountAcquisitionException(Exception e) {
        super("Erro ao buscar configuração do produto [ CAPITAL_LEGACY ] no serviço capital-account-acquisition via gRPC.", e);
    }
}