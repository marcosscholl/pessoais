package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class BuscaCodigoEntidadeNoGestentConectorException extends RuntimeException {
    public BuscaCodigoEntidadeNoGestentConectorException(Exception e) {
        super("Erro ao buscar o código da entidade na api gestent-conector", e);
    }
}