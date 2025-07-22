package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class ProcessamentoOriginacaoFisitalLegadoException extends RuntimeException {

    public ProcessamentoOriginacaoFisitalLegadoException(Exception e) {
        super("Erro ao processar originação fisital-legado", e);
    }
}