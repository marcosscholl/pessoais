package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractHandler {

    protected AbstractHandler proximo;

    public OriginacaoLegado processar(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        var originacaoProcessado = processarProximo(event, originacaoLegado);
        if (proximo != null) {
            return proximo.processar(event, originacaoProcessado);
        }
        return originacaoProcessado;
    }

    public synchronized void setProximo(AbstractHandler proximo) {
        this.proximo = proximo;
    }

    public abstract OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado);
}
