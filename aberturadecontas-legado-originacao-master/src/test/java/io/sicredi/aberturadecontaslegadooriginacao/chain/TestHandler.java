package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;

public class TestHandler extends AbstractHandler {
    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        return originacaoLegado;
    }
}
