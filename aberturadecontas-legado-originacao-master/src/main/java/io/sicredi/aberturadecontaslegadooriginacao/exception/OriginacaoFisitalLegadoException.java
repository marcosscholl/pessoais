package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class OriginacaoFisitalLegadoException extends RuntimeException {
    public OriginacaoFisitalLegadoException(Exception ex) {
        super("Erro ao processar Originação Fisital Legado.", ex);
    }
}
